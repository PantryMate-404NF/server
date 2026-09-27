pipeline {
  agent { label 'jenkins-agent' }

  environment {
    AWS_REGION   = 'ap-northeast-2'
    ECR_REGISTRY = '542119828072.dkr.ecr.ap-northeast-2.amazonaws.com'
    IMAGE_TAG    = "${GIT_COMMIT[0..7]}"
    GITOPS_REPO  = 'https://github.com/PantryMate-404NF/pantry-mate-gitops.git'
    GITOPS_PATH  = 'environments/dev/cloud-test-back'
  }

  options {
    timeout(time: 30, unit: 'MINUTES')
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
        sh 'git log --oneline -3'
      }
    }

    stage('ECR Login') {
      steps {
        container('dind') {
          sh '''
            apk add --no-cache python3 py3-pip
            pip3 install awscli --break-system-packages --quiet
            aws ecr get-login-password --region $AWS_REGION \
              | docker login --username AWS --password-stdin $ECR_REGISTRY
          '''
        }
      }
    }

    stage('Build & Push (1/2)') {
      parallel {

        stage('gateway') {
          steps {
            container('dind') {
              sh '''
                REPO=pantry-mate-dev-gateway
                if aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1; then
                  echo "Image $REPO:$IMAGE_TAG already in ECR, skipping"
                else
                  docker build -f platform/gateway-service/Dockerfile -t $ECR_REGISTRY/$REPO:$IMAGE_TAG .
                  docker push $ECR_REGISTRY/$REPO:$IMAGE_TAG || \
                    aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1 || \
                    { echo "Push failed and image not in ECR"; exit 1; }
                fi
                MANIFEST=$(aws ecr batch-get-image --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG --query 'images[0].imageManifest' --output text)
                aws ecr put-image --region $AWS_REGION --repository-name $REPO --image-tag latest --image-manifest "$MANIFEST" || true
              '''
            }
          }
        }

        stage('user') {
          steps {
            container('dind') {
              sh '''
                REPO=pantry-mate-dev-user
                if aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1; then
                  echo "Image $REPO:$IMAGE_TAG already in ECR, skipping"
                else
                  docker build -f services/user-service/Dockerfile -t $ECR_REGISTRY/$REPO:$IMAGE_TAG .
                  docker push $ECR_REGISTRY/$REPO:$IMAGE_TAG || \
                    aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1 || \
                    { echo "Push failed and image not in ECR"; exit 1; }
                fi
                MANIFEST=$(aws ecr batch-get-image --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG --query 'images[0].imageManifest' --output text)
                aws ecr put-image --region $AWS_REGION --repository-name $REPO --image-tag latest --image-manifest "$MANIFEST" || true
              '''
            }
          }
        }

        stage('product') {
          steps {
            container('dind') {
              sh '''
                REPO=pantry-mate-dev-product
                if aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1; then
                  echo "Image $REPO:$IMAGE_TAG already in ECR, skipping"
                else
                  docker build -f services/product-service/Dockerfile -t $ECR_REGISTRY/$REPO:$IMAGE_TAG .
                  docker push $ECR_REGISTRY/$REPO:$IMAGE_TAG || \
                    aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1 || \
                    { echo "Push failed and image not in ECR"; exit 1; }
                fi
                MANIFEST=$(aws ecr batch-get-image --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG --query 'images[0].imageManifest' --output text)
                aws ecr put-image --region $AWS_REGION --repository-name $REPO --image-tag latest --image-manifest "$MANIFEST" || true
              '''
            }
          }
        }

      }
    }

    stage('Build & Push (2/2)') {
      parallel {

        stage('order-payment') {
          steps {
            container('dind') {
              sh '''
                REPO=pantry-mate-dev-order-payment
                if aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1; then
                  echo "Image $REPO:$IMAGE_TAG already in ECR, skipping"
                else
                  docker build -f services/order-payment-service/Dockerfile -t $ECR_REGISTRY/$REPO:$IMAGE_TAG .
                  docker push $ECR_REGISTRY/$REPO:$IMAGE_TAG || \
                    aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1 || \
                    { echo "Push failed and image not in ECR"; exit 1; }
                fi
                MANIFEST=$(aws ecr batch-get-image --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG --query 'images[0].imageManifest' --output text)
                aws ecr put-image --region $AWS_REGION --repository-name $REPO --image-tag latest --image-manifest "$MANIFEST" || true
              '''
            }
          }
        }

        stage('pantry-recipe') {
          steps {
            container('dind') {
              sh '''
                REPO=pantry-mate-dev-pantry-recipe
                if aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1; then
                  echo "Image $REPO:$IMAGE_TAG already in ECR, skipping"
                else
                  docker build -f Dockerfile.pantry-recipe -t $ECR_REGISTRY/$REPO:$IMAGE_TAG .
                  docker push $ECR_REGISTRY/$REPO:$IMAGE_TAG || \
                    aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1 || \
                    { echo "Push failed and image not in ECR"; exit 1; }
                fi
                MANIFEST=$(aws ecr batch-get-image --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG --query 'images[0].imageManifest' --output text)
                aws ecr put-image --region $AWS_REGION --repository-name $REPO --image-tag latest --image-manifest "$MANIFEST" || true
              '''
            }
          }
        }

        stage('notification') {
          steps {
            container('dind') {
              sh '''
                REPO=pantry-mate-dev-notification
                if aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1; then
                  echo "Image $REPO:$IMAGE_TAG already in ECR, skipping"
                else
                  docker build -f services/notification-service/Dockerfile -t $ECR_REGISTRY/$REPO:$IMAGE_TAG .
                  docker push $ECR_REGISTRY/$REPO:$IMAGE_TAG || \
                    aws ecr describe-images --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG > /dev/null 2>&1 || \
                    { echo "Push failed and image not in ECR"; exit 1; }
                fi
                MANIFEST=$(aws ecr batch-get-image --region $AWS_REGION --repository-name $REPO --image-ids imageTag=$IMAGE_TAG --query 'images[0].imageManifest' --output text)
                aws ecr put-image --region $AWS_REGION --repository-name $REPO --image-tag latest --image-manifest "$MANIFEST" || true
              '''
            }
          }
        }

      }
    }

    stage('Update GitOps') {
      when { branch 'main' }
      steps {
        container('dind') {
          withCredentials([usernamePassword(
            credentialsId: 'github-credentials',
            usernameVariable: 'GIT_USER',
            passwordVariable: 'GIT_TOKEN'
          )]) {
            sh '''
              apk add --no-cache git sed

              git clone https://$GIT_USER:$GIT_TOKEN@$(echo $GITOPS_REPO | sed 's|https://||') gitops-repo
              cd gitops-repo
              git config user.email "jenkins@pantry-mate.internal"
              git config user.name "Jenkins CI"

              for SVC in gateway user product order-payment pantry-recipe notification; do
                REPO="pantry-mate-dev-${SVC}"
                sed -i "s|image: $ECR_REGISTRY/$REPO:.*|image: $ECR_REGISTRY/$REPO:$IMAGE_TAG|g" \
                  $GITOPS_PATH/deployment-${SVC}.yaml
              done

              git add $GITOPS_PATH/
              git diff --cached --quiet || git commit -m "ci: update backend images to $IMAGE_TAG [skip ci]"
              git pull --rebase origin main
              git push origin main
            '''
          }
        }
      }
      post {
        always {
          container('dind') {
            sh 'rm -rf gitops-repo'
          }
        }
      }
    }

  }

  post {
    success  { echo "backend 빌드 완료: ${IMAGE_TAG}" }
    failure  { echo "파이프라인 실패 — 로그를 확인하세요." }
    cleanup  {
      container('dind') {
        sh '''
          for SVC in gateway user product order-payment pantry-recipe notification; do
            docker rmi $ECR_REGISTRY/pantry-mate-dev-${SVC}:$IMAGE_TAG || true
          done
        '''
      }
      cleanWs()
    }
  }
}
