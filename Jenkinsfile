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
                    // Выполняем сборку и запускаем тесты.
                    // Флаг -B нужен для неинтерактивного режима (полезно для CI)
                    sh 'mvn clean test -B'
                }
            }
        }

        stage('Backend: Allure Report') {
            steps {
                dir('backend') {
                    echo 'Generating Allure Report...'
                    // Генерируем статический HTML-отчет
                    sh 'mvn allure:report -B'
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
                    // Используйте sh 'npm install' или sh 'npm ci', если у вас установлен Node.js плагин
                    // NodeJS плагин нужно настроить в Jenkins (Global Tool Configuration)

                    // Пример (раскомментируйте, если нужно):
                    // sh 'npm ci'
                    // sh 'npm run build'
                    // sh 'npm run test'
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
