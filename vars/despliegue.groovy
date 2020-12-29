def call(stageOptions){
  
        def ejecutarBuild = false;
        stage("compile"){   
            env.TAREA =  env.STAGE_NAME 
            ejecutarBuild =true;
            echo 'stage compile'
            sh 'mvn clean compile -e'         
        }
        stage("unitTest"){   
            env.TAREA =  env.STAGE_NAME
            echo 'stage unitTest' 
            sh 'mvn clean test -e'          
        }
        stage("jar"){   
            env.TAREA =  env.STAGE_NAME 
            echo 'stage jar'
            sh 'mvn clean package -e'          
        }

        stage("sonar"){
            env.TAREA =  env.STAGE_NAME 
            echo 'stage sonar'
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
        stage("nexusUpload"){    
            env.TAREA =  env.STAGE_NAME  
            echo 'stage nexusUpload' 
            if ((stageOptions.contains('Nexus') || (stageOptions =='')) && (buildEjecutado) )      
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: 'jar', filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020', groupId: 'com.devopsusach2020', packaging: 'jar', version: '2.0.1']]]                     
        } 
        stage("gitCreateRelease"){    
            env.TAREA =  env.STAGE_NAME  
            echo 'stage gitCreateRelease' 

        } 
        stage("gitDiff"){    
            env.TAREA =  env.STAGE_NAME  
            echo 'stage gitDiff' 

        } 
        stage("nexusDownload"){    
            env.TAREA =  env.STAGE_NAME  
            echo 'stage nexusDownload'              
            sh 'curl -X GET -u admin:Pch1axli3003 http://localhost:9000/repository/test-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar -O'
        } 
        stage("run"){
            env.TAREA =  env.STAGE_NAME 
            echo 'stage run'
            if ((stageOptions.contains('Run') || (stageOptions =='')) && (buildEjecutado) ){ 
                sh "nohup bash gradlew bootRun &"
                sleep 20 
            }                          
        }
        stage("test"){
            env.TAREA =  env.STAGE_NAME 
            echo 'stage test'
            if ((stageOptions.contains('Rest') || (stageOptions =='')) && (buildEjecutado) ) 
                sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
        }  

         stage("gitMergeMaster"){    
            env.TAREA =  env.STAGE_NAME  
            echo 'stage gitMergeMaster' 

        } 
        stage("gitMergeDevelop"){    
            env.TAREA =  env.STAGE_NAME   
            echo 'stage gitMergeDevelop'

        } 
        stage("gitTagMaster"){    
            env.TAREA =  env.STAGE_NAME 
            echo 'stage gitTagMaster'  

        } 

             

}

return this;