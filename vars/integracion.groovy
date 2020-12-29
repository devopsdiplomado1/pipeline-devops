def call(stageOptions){
  
        def buildEjecutado = false;

        stage("compile"){   
            env.TAREA =  env.STAGE_NAME   
            echo "XXXXXXXXX.-Rama ${env.BRANCH_NAME}"           
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
            buildEjecutado =true;          
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
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: 'jar', filePath: 'build/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020', groupId: 'com.devopsusach2020', packaging: 'jar', version: '2.0.1']]]                     
        } 
        stage("gitCreateRelease"){    
            env.TAREA =  env.STAGE_NAME  
            echo "4.-Rama ${env.BRANCH_NAME}" 
            echo 'stage gitCreateRelease'

            if ((stageOptions.contains('gitCreateRelease') || (stageOptions =='')) && (buildEjecutado) && (env.BRANCH_NAME.contains('develop')) )
                echo 'creando gitCreateRelease' 

            

        }

             

}

return this;