
def call(stageOptions, nameProject){
   def buildEjecutado = false;
   def projectKey = "${nameProject}-${env.BRANCH_NAME}-${env.BUILD_ID}"
   def nameRelease = "release-v1-0-0"
   int contStages = 0;
  
         stage("Validar"){

           if (
                stageOptions.contains('Compile')           ||
                stageOptions.contains('unitTest')          ||
                stageOptions.contains('jar')               ||
                stageOptions.contains('sonar')             ||
                stageOptions.contains('nexusUpload')       || 
                (stageOptions =='')
               ) {
               echo "Ok, se continua con los stage, ya que ingreso parametros conocidos"
            } else {
                currentBuild.result = 'FAILURE'
                error ('No se puede ejecutar este pipeline, ya que no ingreso parametros conocidos')
            }   

       }

        stage('Compile') {
                env.TAREA =  env.STAGE_NAME 
                buildEjecutado =false;
                echo "STAGE ${env.STAGE_NAME}"
                if (stageOptions.contains('Compile Code') || (stageOptions ==''))  { 
                    sh 'mvn clean compile -e'    
                    contStages++                
                }
        }

        stage('unitTest') {     
                env.TAREA =  env.STAGE_NAME 
                echo "STAGE ${env.STAGE_NAME}" 
                if ((stageOptions.contains('Test') || (stageOptions =='')) ) {      
                    sh 'mvn clean test -e'
                    contStages++
                } 
        }
        stage('jar') {        
                env.TAREA =  env.STAGE_NAME 
                echo "STAGE ${env.STAGE_NAME}"
                if ((stageOptions.contains('Test') || (stageOptions ==''))) {      
                    sh 'mvn clean package -e' 
                    buildEjecutado =true;
                    contStages++
                }
        }
        stage('sonar') {
            env.TAREA =  env.STAGE_NAME 
            echo "STAGE ${env.STAGE_NAME}"
            if (!buildEjecutado) {
                currentBuild.result = 'FAILURE'
                echo "No se puede ejecutar Sonar sin haber ejecutado un Build"
                buildEjecutado = false;
                
            }    

            def scannerHome = tool 'sonar-scanner';    
            withSonarQubeEnv('sonar-server') { 
                if ((stageOptions.contains('Sonar') || (stageOptions =='')) && (buildEjecutado) ) {                 
                    echo "Aplicando Sonar al proyecto:${projectKey}"
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectKey} -Dsonar.java.binaries=build"  
                    contStages++ 
                   }    
            }  
            script {
                try {
                    timeout(1) {
                        def qg = waitForQualityGate('sonar')

                        if (!qg.contains('UP')) {
                            Error "Fallo el llamado a Sonar, se espero 1 minuto, cambie el tiempo si es necesario o verifique la instalacion! failure: ${qg.status}"
                        }
                    }    
                } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
                    currentBuild.result = "FAILURE"
                    env.shouldBuild = "false"
                }
            }    
          


        }         

        stage('nexusUpload') {  
            env.TAREA =  env.STAGE_NAME 
            echo "STAGE ${env.STAGE_NAME}"  
            if ((stageOptions.contains('nexusUpload') || (stageOptions =='')) && (buildEjecutado) && (contStages == 4)) {         
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: 'jar', filePath: 'build/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020', groupId: 'com.devopsusach2020', packaging: 'jar', version: '0.0.1']]]                     
                contStages++
            }   
        }  

        stage('gitCreateRelease') {  
            //rodrigo
            if ( ("${env.BRANCH_NAME}" =~ /(develop)/) && (stageOptions.contains('gitCreateRelease') || (stageOptions =='')) && (buildEjecutado) && (contStages == 5)) {
                env.TAREA =  env.STAGE_NAME 
                echo "STAGE ${env.STAGE_NAME}"
                echo "entro a gitCreateRelease" 
                

                //if (gitUtils.chequearSiExisteRama("${nameRelease}")) {
                echo "Se borra rama <${nameRelease}>"
                gitUtils.borrarRama("${nameRelease}")
                //}

                echo "Se crea rama <${nameRelease}>"
                gitUtils.crearRamaGit("${env.GIT_BRANCH}", "${nameRelease}");
                if (gitUtils.chequearSiExisteRama("${nameRelease}")) {
                        echo "Rama <${nameRelease}> creada correctamente"
                        echo "Ahora se llama a despliegue continuo..."
                        despliegue.call(stageOptions, nameProject);
                } else {    
                        currentBuild.result = 'FAILURE'
                        error ('Rama <${nameRelease}> no se creo')
                }
                 
                
                //Este stage sólo debe estar disponible para la rama develop.  
                //Si IC de develop OK = ejecución de pipeline CD para rama Release creada  
            }         
        }                    

}

return this;