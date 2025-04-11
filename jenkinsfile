pipeline {
    agent any

    environment {
        PROJECT_NAME = 'my-pizza'
        DOCKER_HUB_USER = 'aramidhwan' // ?? 사용자에 맞게 수정
        IMAGE_TAG = "latest" // 필요시 PR 번호나 커밋 해시로 바꿔도 됨
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
                    def changedFiles = sh(script: "git diff --name-only origin/main", returnStdout: true).trim().split('\n')

                    echo "?? 변경된 파일 목록:"
                    changedFiles.each { echo it }

                    def BUILD_COMMON   = changedFiles.any { it.startsWith("common-service/") }
                    def BUILD_ORDER    = changedFiles.any { it.startsWith("order-service/") }
                    def BUILD_STORE    = changedFiles.any { it.startsWith("store-service/") }
                    def BUILD_DELIVERY = changedFiles.any { it.startsWith("delivery-service/") }
                    def BUILD_MYPAGE   = changedFiles.any { it.startsWith("mypage-service/") }
                }
            }
        }

        stage('Build Services') {
            steps {
                script {
                    if (BUILD_COMMON)   { echo '?? common-service 빌드'   /* sh 'cd common-service && ./gradlew build' */ }
                    if (BUILD_ORDER)    { echo '?? order-service 빌드'    /* sh 'cd order-service && ./gradlew build' */ }
                    if (BUILD_STORE)    { echo '?? store-service 빌드'    /* sh 'cd store-service && ./gradlew build' */ }
                    if (BUILD_DELIVERY) { echo '?? delivery-service 빌드' /* sh 'cd delivery-service && ./gradlew build' */ }
                    if (BUILD_MYPAGE)   { echo '?? mypage-service 빌드'   /* sh 'cd mypage-service && ./gradlew build' */ }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'forGitHub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        """

                        if (BUILD_COMMON) {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/common-service:${IMAGE_TAG} ./common-service
                                docker push ${DOCKER_HUB_USER}/common-service:${IMAGE_TAG}
                            """
                        }
                        if (BUILD_ORDER) {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/order-service:${IMAGE_TAG} ./order-service
                                docker push ${DOCKER_HUB_USER}/order-service:${IMAGE_TAG}
                            """
                        }
                        if (BUILD_STORE) {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/store-service:${IMAGE_TAG} ./store-service
                                docker push ${DOCKER_HUB_USER}/store-service:${IMAGE_TAG}
                            """
                        }
                        if (BUILD_DELIVERY) {
                            sh """
                                docker build -t ${DOCKER_HUB_USER}/delivery-service:${IMAGE_TAG} ./delivery-service
                                docker push ${DOCKER_HUB_USER}/delivery-service:${IMAGE_TAG}
                            """
                        }
                        if (BUILD_MYPAGE) {
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
            echo '? 전체 파이프라인 완료'
        }
        failure {
            echo '? 오류 발생 - 로그를 확인하세요'
        }
    }
}
