pipeline {
  agent {
    docker {
      image 'maven:3.5.2-jdk-8'
    }
  }
  environment {
      stagingPostgresDbUrl = credentials('EGAPOSTGRESDBURL')
      fallBackPostgresDbUrl = credentials('EGAPOSTGRESDBURL')
      productionPostgresDbUrl = credentials('EGAPOSTGRESDBURL')
      postgresDBUserName = credentials('POSTGRESDBUSERNAME')
      postgresDBPassword = credentials('POSTGRESDBPASSWORD')
      tomcatCredentials = credentials('TOMCATCREDENTIALS')
      stagingHost = credentials('STAGINGHOST')
      fallbackHost = credentials('FALLBACKHOST')
      productionHost = credentials('PRODUCTIONHOST')
   }
   parameters {
      choice(choices: ['validate', 'create'], description: 'Behaviour at connection time (initialize/validate schema)',
      name: 'DbBehaviour')
      booleanParam(name: 'DeployToStaging' , defaultValue: false , description: '')
      booleanParam(name: 'DeployToProduction' , defaultValue: false , description: '')
    }
  stages {
    stage('Default Build pointing to Staging DB') {
      steps {
        sh "mvn clean package -DskipTests -DbuildDirectory=staging/target -DegaAcccession-db.url=${stagingPostgresDbUrl} -DegaAccession-db.username=${postgresDBUserName} -DegaAcccession-db.password=${postgresDBPassword} -Dinstance.id=ega-accession-01-staging -Dddl-behaviour=${params.DbBehaviour}"
      }
    }
    stage('Build For FallBack And Production') {
      when {
        expression {
          params.DeployToProduction == true
         }
       }
      steps {
        echo 'Build pointing to FallBack DB'
        sh "mvn clean package -DskipTests -DbuildDirectory=fallback/target -DegaAcccession-db.url=${fallBackPostgresDbUrl} -DegaAccession-db.username=${postgresDBUserName} -DegaAcccession-db.password=${postgresDBPassword} -Dinstance.id=ega-accession-01-fallback -Dddl-behaviour=${params.DbBehaviour}"
        echo 'Build pointing to Production DB'
        sh "mvn clean package -DskipTests -DbuildDirectory=production/target -DegaAcccession-db.url=${productionPostgresDbUrl} -DegaAccession-db.username=${postgresDBUserName} -DegaAcccession-db.password=${postgresDBPassword} -Dinstance.id=ega-accession-01-production -Dddl-behaviour=${params.DbBehaviour}"
       }
     }
    stage('Deploy To Staging') {
      when {
        expression {
          params.DeployToStaging == true
         }
       }
      steps {
        echo 'Deploying to Staging'
        sh "curl --upload-file staging/target/accessioning-service*.war 'http://'${tomcatCredentials}'@'${stagingHost}':8080/manager/text/deploy?path=/ega/accession&update=true' | grep 'OK - Deployed application at context path '"
       }
     }
    stage('Deploy To FallBack And Production') {
      when {
        expression {
          params.DeployToProduction == true
         }
       }
      steps {
        echo 'Deploying to Fallback'
        sh "curl --upload-file fallback/target/accessioning-service*.war 'http://'${tomcatCredentials}'@'${fallbackHost}':8080/manager/text/deploy?path=/ega/accession&update=true' | grep 'OK - Deployed application at context path '"
        echo 'Deploying to Production'
        sh "curl --upload-file production/target/accessioning-service*.war 'http://'${tomcatCredentials}'@'${productionHost}':8080/manager/text/deploy?path=/ega/accession&update=true' | grep 'OK - Deployed application at context path '"
        archiveArtifacts artifacts: 'production/target/*.war' , fingerprint: true
       }
     }
   }
 }
