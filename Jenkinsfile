pipeline {
    agent any

    environment {
        PROJECT_NAME = 'my-pizza'
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
                    // PR 또는 브랜치에서 변경된 파일 경로 추출
                    changedFiles = sh(script: "git diff --name-only origin/main", returnStdout: true).trim().split('\n')
                    
                    echo "📁 변경된 파일 목록:"
                    changedFiles.each { echo it }

                    // 변경된 서브폴더 플래그 설정
                    BUILD_COMMON     = changedFiles.any { it.startsWith("common-service/") }
                    BUILD_ORDER     = changedFiles.any { it.startsWith("order-service/") }
                    BUILD_STORE     = changedFiles.any { it.startsWith("store-service/") }
                    BUILD_DELIVERY    = changedFiles.any { it.startsWith("delivery-service/") }
                    BUILD_MYPAGE   = changedFiles.any { it.startsWith("mypage-service/") }
                }
            }
        }

        stage('Build Changed Services') {
            steps {
                script {
                    if (BUILD_COMMON) {
                        echo '🚀 common-service 빌드 중...'
                        // sh 'cd common-service && ./gradlew build'
                    }
                    if (BUILD_ORDER) {
                        echo '🚀 order-service 빌드 중...'
                        // sh 'cd order-service && ./gradlew build'
                    }
                    if (BUILD_STORE) {
                        echo '🚀 store-service 빌드 중...'
                        // sh 'cd store-service && ./gradlew build'
                    }
                    if (BUILD_DELIVERY) {
                        echo '🚀 delivery-service 빌드 중...'
                        // sh 'cd delivery-service && ./gradlew build'
                    }
                    if (BUILD_MYPAGE) {
                        echo '🚀 mypage-service 빌드 중...'
                        // sh 'cd mypage-service && ./gradlew build'
                    }

                    if (!(BUILD_COMMON || BUILD_ORDER || BUILD_DELIVERY || BUILD_STORE || BUILD_MYPAGE)) {
                        echo 'ℹ️ 변경된 서비스가 없어 빌드할 것이 없습니다.'
                    }
                }
            }
        }
    }
}
