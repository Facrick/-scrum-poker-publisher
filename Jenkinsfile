pipeline {
    agent any

    tools {
        // Убедитесь, что в настройках Jenkins (Global Tool Configuration)
        // настроен Maven с именем 'Maven' и Java с именем 'Java 21' (или аналогичными)
        maven 'Maven'
        jdk 'Java 21'
    }

    stages {
        stage('Checkout') {
            steps {
                // Jenkins автоматически делает checkout репозитория,
                // если настроен Multibranch Pipeline или Pipeline from SCM
                echo 'Checking out code...'
                checkout scm
            }
        }

        stage('Backend: Build & Test') {
            steps {
                dir('backend') {
                    echo 'Running Maven build and tests...'
                    // Используем bat вместо sh для Windows
                    bat 'mvn clean test -B'
                }
            }
        }

        stage('Backend: Allure Report') {
            steps {
                dir('backend') {
                    echo 'Generating Allure Report...'
                    // Используем bat вместо sh для Windows
                    bat 'mvn allure:report -B'
                }
            }
            post {
                always {
                    // Публикуем отчет в Jenkins.
                    // Для этого в Jenkins должен быть установлен плагин Allure.
                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'backend/target/allure-results']]
                    ])
                }
            }
        }

        // Если у вас есть тесты на фронтенде, можно добавить этот стейдж
        stage('Frontend: Build & Test') {
            steps {
                dir('frontend') {
                    echo 'Installing npm dependencies...'
                    // Пример (раскомментируйте, если нужно):
                    // bat 'npm ci'
                    // bat 'npm run build'
                    // bat 'npm run test'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully! 🎉'
        }
        failure {
            echo 'Pipeline failed. 😢 Check the logs for details.'
        }
    }
}
