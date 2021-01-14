    def call(stageOptions, nameProject){
  
        def downloadOK = false;

         stage("Validar"){

           if (
                stageOptions.contains('gitDiff')            ||
                stageOptions.contains('nexusDownload')      ||
                stageOptions.contains('run')                ||
                stageOptions.contains('test')               ||
                stageOptions.contains('gitMergeMaster')     || 
                stageOptions.contains('gitMergeDevelop')    ||
                stageOptions.contains('gitTagMaster')       ||
                (stageOptions =='')
               ) {
               echo "Ok, se continua con los stage, ya que ingreso parametros conocidos"
            } else {
                currentBuild.result = 'FAILURE'
                error ('No se puede ejecutar este pipeline, ya que no ingreso parametros conocidos')
            }   

       }


        stage("gitDiff"){   
            //Andres 
            env.TAREA =  env.STAGE_NAME  
            echo 'stage gitDiff' 

        } 
        stage("nexusDownload"){   
            //rodrigo 
            env.TAREA =  env.STAGE_NAME  
            echo 'stage nexusDownload'   
            if ((stageOptions.contains('nexusDownload') || (stageOptions =='')) ) {           
                sh 'curl -X GET -u admin:admin http://localhost:8081/repository/test-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar -O' 
                downloadOK = true;
            } 

            if (!downloadOK){
                currentBuild.result = 'FAILURE'
                error ('No se puede seguir ejecutando este pipeline, ya que no se descargo el artefacto')
            }   
        } 
        stage("run"){
            //rodrigo
            env.TAREA =  env.STAGE_NAME 
            echo 'stage run'  

            if ((stageOptions.contains('run') || (stageOptions =='')) && (downloadOK)){ 
                sh "java -jar DevOpsUsach2020-0.0.1.jar &"
                sleep 20 
            }                          
        }
        stage("test"){
            //rodrigo
            env.TAREA =  env.STAGE_NAME 
            echo 'stage test'
            if ((stageOptions.contains('test') || (stageOptions ==''))  ) 
                sh 'curl -X GET "http://localhost:8080/rest/mscovid/test?msg=testing"'
        }  

         stage("gitMergeMaster"){   
             //cesar 
            env.TAREA =  env.STAGE_NAME  
            echo 'stage gitMergeMaster' 

        } 
        stage("gitMergeDevelop"){  
             //cesar  
            env.TAREA =  env.STAGE_NAME   
            echo 'stage gitMergeDevelop'

        } 
        stage("gitTagMaster"){    
             //Joram
            env.TAREA =  env.STAGE_NAME 
            echo 'stage gitTagMaster'  

        } 

             

}

return this;