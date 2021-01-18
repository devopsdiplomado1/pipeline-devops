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
            env.TAREA = env.STAGE_NAME

            gitUtils.referenciaMain()
            gitUtils.diffRama("${env.GIT_BRANCH}")            
            contStages++;
        }

        stage("nexusDownload"){   
            env.TAREA =  env.STAGE_NAME  
            echo 'stage nexusDownload'   
            if ((stageOptions.contains('nexusDownload') || (stageOptions =='')) ) {
                /*Configurar puerto según el que se utiliza en cada pc local para iniciar nexus*/     
                sh 'curl -X GET -u admin:admin http://localhost:9081/repository/test-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar -O'
                downloadOK = true;
            } 

            if (!downloadOK){
                currentBuild.result = 'FAILURE'
                error ('No se puede seguir ejecutando este pipeline, ya que no se descargo el artefacto')
            }
            contStages++;
        } 
        stage("run"){
            env.TAREA =  env.STAGE_NAME 
            echo 'stage run'  

            if ((stageOptions.contains('run') || (stageOptions =='')) && (downloadOK)){ 
                sh "java -jar DevOpsUsach2020-0.0.1.jar --server.port=8888 &"
                sleep 20 
            }    
            contStages++;                       
        }
        stage("test"){
            env.TAREA =  env.STAGE_NAME 
            echo 'stage test'
            if ((stageOptions.contains('test') || (stageOptions ==''))  ) 
                sh 'curl -X GET "http://localhost:8888/rest/mscovid/test?msg=testing"'
            contStages++;
        }  

        stage("gitMergeMaster"){   
            env.TAREA =  env.STAGE_NAME  
            echo 'stage gitMergeMaster' 
            if ((stageOptions.contains('gitMergeMaster') || (stageOptions ==''))  ) {
                if(contStages == 4){
                    gitUtils.crearMerge("origin/${env.GIT_BRANCH}", "origin/main")
                    contStages++;
                } else {
                    echo  "No se ejecuta merge en main, ya que no se han ejecutado todos los stages"
                }
            }
        } 
        stage("gitMergeDevelop"){  
            env.TAREA =  env.STAGE_NAME   
            echo 'stage gitMergeDevelop'
            if ((stageOptions.contains('gitMergeDevelop') || (stageOptions ==''))  ) {
                if(contStages == 5){
                    gitUtils.referenciaDevelop()
                    gitUtils.crearMerge("origin/main", "origin/develop")
                    contStages++;
                } else {
                    echo "No se ejecuta merge en develop, ya que no se han ejecutado todos los stages"
                }
            }
        } 
        stage("gitTagMaster"){    
            env.TAREA =  env.STAGE_NAME   
            echo 'Tag Main: ${tag}' 
            if(contStages == 6){
                if ((stageOptions.contains('gitTagMaster') || (stageOptions =='')) ) {    
                    gitUtils.referenciaRelease("${env.GIT_BRANCH}")   
                    gitUtils.referenciaMain()            
                    gitUtils.tagGit("origin/main", "origin/${env.GIT_BRANCH}")
                } 
            } else {
                echo "No se ejecuta gitTagMaster, ya que no se han ejecutado todos los stages"
            }   
            
        } 

             

}

return this;
