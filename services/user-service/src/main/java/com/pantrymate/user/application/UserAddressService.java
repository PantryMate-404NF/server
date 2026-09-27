package com.pantrymate.user.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.domain.UserAddress;
import com.pantrymate.user.domain.UserAddressRepository;
import com.pantrymate.user.domain.UserRepository;
import com.pantrymate.user.domain.exception.UserErrorCode;
import com.pantrymate.user.presentation.dto.UserAddressCreateRequestDto;
import com.pantrymate.user.presentation.dto.UserAddressResponseDto;
import com.pantrymate.user.presentation.dto.UserAddressUpdateRequestDto;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAddressService {

    private static final int MAX_ADDRESSES = 10;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_ADDRESS_LENGTH = 200;
    private static final Pattern PHONE = Pattern.compile("^01[016789]\\d{7,8}$");
    private static final Pattern ZIP_CODE = Pattern.compile("^\\d{5}$");

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    public UserAddressService(UserAddressRepository userAddressRepository, UserRepository userRepository) {
        this.userAddressRepository = userAddressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserAddressResponseDto> getAll(Long userId) {
        return userAddressRepository.findByUserIdOrderByDefaultAddressDescAddressIdDesc(userId).stream()
                .map(UserAddressResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAddressResponseDto getDefault(Long userId) {
        return userAddressRepository
                .findByUserIdAndDefaultAddressTrue(userId)
                .map(UserAddressResponseDto::from)
                .orElse(null);
    }

    @Transactional
    public UserAddressResponseDto save(Long userId, UserAddressCreateRequestDto request) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(UserErrorCode.USER_NOTFOUND_ID);
        }
        ValidatedAddress input = validate(
                request.recipientName(),
                request.recipientPhone(),
                request.zipCode(),
                request.address(),
                request.addressDetail());

        long count = userAddressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new BusinessException(UserErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }
        boolean makeDefault = count == 0 || Boolean.TRUE.equals(request.isDefault());
        if (makeDefault) {
            userAddressRepository.clearDefault(userId);
        }
        UserAddress saved = userAddressRepository.save(UserAddress.create(
                userId,
                input.recipientName(),
                input.recipientPhone(),
                input.zipCode(),
                input.address(),
                input.addressDetail(),
                makeDefault));
        return UserAddressResponseDto.from(saved);
    }

    @Transactional
    public UserAddressResponseDto update(Long userId, Long addressId, UserAddressUpdateRequestDto request) {
        ValidatedAddress input = validate(
                request.recipientName(),
                request.recipientPhone(),
                request.zipCode(),
                request.address(),
                request.addressDetail());
        UserAddress userAddress = getByIdAndUserId(addressId, userId);
        userAddress.update(
                input.recipientName(), input.recipientPhone(), input.zipCode(), input.address(), input.addressDetail());
        return UserAddressResponseDto.from(userAddress);
    }

    @Transactional
    public UserAddressResponseDto updateDefault(Long userId, Long addressId) {
        getByIdAndUserId(addressId, userId);
        userAddressRepository.clearDefault(userId);
        UserAddress target = getByIdAndUserId(addressId, userId);
        target.markDefault();
        return UserAddressResponseDto.from(target);
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        UserAddress userAddress = getByIdAndUserId(addressId, userId);
        boolean wasDefault = userAddress.isDefaultAddress();
        userAddressRepository.delete(userAddress);
        userAddressRepository.flush();
        if (wasDefault) {
            userAddressRepository
                    .findFirstByUserIdOrderByAddressIdDesc(userId)
                    .ifPresent(UserAddress::markDefault);
        }
    }

    private UserAddress getByIdAndUserId(Long addressId, Long userId) {
        return userAddressRepository
                .findByAddressIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.ADDRESS_NOTFOUND));
    }

    private ValidatedAddress validate(
            String recipientName, String recipientPhone, String zipCode, String address, String addressDetail) {
        String name = recipientName == null ? "" : recipientName.trim();
        String phone = recipientPhone == null ? "" : recipientPhone.replaceAll("[\\s-]", "");
        String zip = zipCode == null ? "" : zipCode.trim();
        String base = address == null ? "" : address.trim();
        String detail = addressDetail == null || addressDetail.isBlank() ? null : addressDetail.trim();

        if (name.isEmpty()
                || name.length() > MAX_NAME_LENGTH
                || !PHONE.matcher(phone).matches()
                || !ZIP_CODE.matcher(zip).matches()
                || base.isEmpty()
                || base.length() > MAX_ADDRESS_LENGTH
                || (detail != null && detail.length() > MAX_ADDRESS_LENGTH)) {
            throw new BusinessException(UserErrorCode.ADDRESS_INVALID_INPUT);
        }
        return new ValidatedAddress(name, phone, zip, base, detail);
    }

    private record ValidatedAddress(
            String recipientName, String recipientPhone, String zipCode, String address, String addressDetail) {}
}
