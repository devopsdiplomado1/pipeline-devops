def call(stageOptions){
  
        def ejecutarBuild = false;
        stage("Build & Test"){   
            env.TAREA =  env.STAGE_NAME 
            ejecutarBuild =false;

            if (stageOptions.contains('Build') || (stageOptions ==''))  {   
                sh "./gradlew clean build -x test" 
                buildEjecutado =true;
            } 
            if ((stageOptions.contains('Test') || (stageOptions =='')) && (buildEjecutado) )     
                sh "./gradlew clean build"   
                //    sh "./gradlew clean build -x build"        
        }
        stage("Sonar"){
            env.TAREA =  env.STAGE_NAME 
            if (!buildEjecutado) {
                currentBuild.result = 'FAILURE'
                echo "No se puede ejecutar Sonar sin haber ejecutado un Build"
            }    

            def scannerHome = tool 'sonar-scanner';    
            withSonarQubeEnv('sonar-server') { 
                if ((stageOptions.contains('Sonar') || (stageOptions =='')) && (buildEjecutado) )
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build"   
            }                        
        }
        stage("Run"){
            env.TAREA =  env.STAGE_NAME 
            if ((stageOptions.contains('Run') || (stageOptions =='')) && (buildEjecutado) ){ 
                sh "nohup bash gradlew bootRun &"
                sleep 20 
            }                          
        }
        stage("Rest"){
            env.TAREA =  env.STAGE_NAME 
            if ((stageOptions.contains('Rest') || (stageOptions =='')) && (buildEjecutado) ) 
                sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
        }  
        stage("Nexus"){    
            env.TAREA =  env.STAGE_NAME   
            if ((stageOptions.contains('Nexus') || (stageOptions =='')) && (buildEjecutado) )      
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: 'jar', filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020', groupId: 'com.devopsusach2020', packaging: 'jar', version: '2.0.1']]]                     
        }  
             

}

return this;