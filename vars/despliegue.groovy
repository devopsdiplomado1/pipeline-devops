    def call(stageOptions, nameProject){
  
        def downloadOK = false;
        int contStages = 0;

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
            env.TAREA = env.STAGE_NAME

            //echo env.GIT_BRANCH
            //DIFF = sh(returnStdout: true, script: "git diff ${env.GIT_BRANCH} origin/main").trim()
            //echo DIFF
            gitUtils.referenciaMain()
            gitUtils.diffRama("${env.GIT_BRANCH}")            
            contStages++;
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
            contStages++;
        } 
        stage("run"){
            //rodrigo
            env.TAREA =  env.STAGE_NAME 
            echo 'stage run'  

            if ((stageOptions.contains('run') || (stageOptions =='')) && (downloadOK)){ 
                //sh "nohup mvn spring-boot:run -Dserver.port=8088 &"
                sh "nohup java -jar DevOpsUsach2020-0.0.1.jar --server.port=8088 &"
                sleep 20 
            }    
            contStages++;                       
        }
        stage("test"){
            //rodrigo
            env.TAREA =  env.STAGE_NAME 
            echo 'stage test'
            if ((stageOptions.contains('test') || (stageOptions ==''))  ) 
                sh 'curl -X GET "http://localhost:8088/rest/mscovid/test?msg=testing"'
            contStages++;
        }  

         stage("gitMergeMaster"){   
             //cesar 
            env.TAREA =  env.STAGE_NAME  
            echo 'stage gitMergeMaster' 
            if(contStages == 4){
                gitUtils.referenciaMain()
                gitUtils.referenciaRelease("${env.GIT_BRANCH}")
                gitUtils.crearMerge("${env.GIT_BRANCH}", "main")
                contStages++;
            } else {
                currentBuild.result = 'FAILURE'
                error ('No se ejecuta merge en main, ya que no se han ejecutado todos los stages')
            }

        } 
        stage("gitMergeDevelop"){  
             //cesar  
            env.TAREA =  env.STAGE_NAME   
            echo 'stage gitMergeDevelop'
            if(contStages == 5){
                gitUtils.referenciaMain()
                gitUtils.referenciaDevelop()
                gitUtils.crearMerge("main", "develop")
                contStages++;
            } else {
                currentBuild.result = 'FAILURE'
                error ('No se ejecuta merge en develop, ya que no se han ejecutado todos los stages')
            }

        } 
        stage("gitTagMaster"){    
            //Joram
            env.TAREA =  env.STAGE_NAME   
            echo 'Tag Main: ${tag}' 
            if(contStages == 6){
                    if ((stageOptions.contains('gitTagMaster') || (stageOptions =='')) ) {    
                        gitUtils.referenciaRelease("${env.GIT_BRANCH}")   
                        gitUtils.referenciaMain()            
                        gitUtils.tagGit("origin/main", "origin/${env.GIT_BRANCH}")
                    } 
            } else {
                currentBuild.result = 'FAILURE'
                error ('No se ejecuta gitTagMaster, ya que no se han ejecutado todos los stages')
            }   
            
        } 

             

}

return this;
