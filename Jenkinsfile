pipeline {
    agent any

    environment {
        PROJECT_NAME = 'my-pizza'
        DOCKER_HUB_USER = 'aramidhwan'
        IMAGE_TAG = "latest"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim().split('\n')

                    echo "📁 변경된 파일 목록:"
                    changedFiles.each { echo it }
                    
                    if (changedFiles.size() == 0) {
                        echo '⚠️ 변경된 파일이 없습니다.'
                    } else {
                        echo "✅ 변경 감지됨!"
                    }

                    env.BUILD_COMMON   = changedFiles.any { it.startsWith("common/") }.toString()
                    env.BUILD_ORDER    = changedFiles.any { it.startsWith("order/") }.toString()
                    env.BUILD_STORE    = changedFiles.any { it.startsWith("store/") }.toString()
                    env.BUILD_DELIVERY = changedFiles.any { it.startsWith("delivery/") }.toString()
                    env.BUILD_MYPAGE   = changedFiles.any { it.startsWith("customercenter/") }.toString()
                }
            }
        }

        stage('Build Services') {
            steps {
                script {
                    if (env.BUILD_COMMON == 'true')   { echo '🚧 common-service 빌드'   /* sh 'cd common-service && ./gradlew build' */ }
                    if (env.BUILD_ORDER == 'true')    { echo '🚧 order-service 빌드'    /* sh 'cd order-service && ./gradlew build' */ }
                    if (env.BUILD_STORE == 'true')    { echo '🚧 store-service 빌드'    /* sh 'cd store-service && ./gradlew build' */ }
                    if (env.BUILD_DELIVERY == 'true') { echo '🚧 delivery-service 빌드' /* sh 'cd delivery-service && ./gradlew build' */ }
                    if (env.BUILD_MYPAGE == 'true')   { echo '🚧 mypage-service 빌드'   /* sh 'cd mypage-service && ./gradlew build' */ }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credential', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        '''

                        if (env.BUILD_COMMON == 'true') {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/common-service:${IMAGE_TAG} ./common-service
                                docker push ${DOCKER_HUB_USER}/common-service:${IMAGE_TAG}
                            """
                        }
                        if (env.BUILD_ORDER == 'true') {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/order-service:${IMAGE_TAG} ./order-service
                                docker push ${DOCKER_HUB_USER}/order-service:${IMAGE_TAG}
                            """
                        }
                        if (env.BUILD_STORE == 'true') {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/store-service:${IMAGE_TAG} ./store-service
                                docker push ${DOCKER_HUB_USER}/store-service:${IMAGE_TAG}
                            """
                        }
                        if (env.BUILD_DELIVERY == 'true') {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/delivery-service:${IMAGE_TAG} ./delivery-service
                                docker push ${DOCKER_HUB_USER}/delivery-service:${IMAGE_TAG}
                            """
                        }
                        if (env.BUILD_MYPAGE == 'true') {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/mypage-service:${IMAGE_TAG} ./mypage-service
                                docker push ${DOCKER_HUB_USER}/mypage-service:${IMAGE_TAG}
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ 전체 파이프라인 완료'
        }
        failure {
            echo '❌ 오류 발생 - 로그를 확인하세요'
        }
    }
}
