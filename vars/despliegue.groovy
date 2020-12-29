def call(stageOptions){
  
        def ejecutarBuild = false;
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