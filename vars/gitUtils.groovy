def crearRamaGit(String origin, String newRranch){
    try {
        sh '''
            git fetch -p
            git checkout '''+origin+'''; git pull
            git checkout -b '''+newRranch+'''
            git push origin '''+newRranch+'''
        '''
    } catch (Exception a){ } 


}

def borrarRama(String rama){
    
    try {
        sh "git fetch -p"
        sh "git branch -d  ${rama}"
    } catch (Exception a){ }   

    try {
        sh "git push origin --delete ${rama}"
        sh "git fetch -p"
    } catch (Exception a){ }      
    
}

def listarRamas(){

    def getBranches = ("git ls-remote https://github.com/devopsdiplomado1/ms-iclab.git").execute()

    def branchNameList = getBranches.text.readLines().collect {
     it.split()[1]
    }
    def releaseBranchList = branchNameList.findAll { it =~ /refs\/heads\/release*/ }

    def humanReadableReleaseBranchList = releaseBranchList.collect {
    it.replaceAll('refs/heads/', '')
    }
    echo "${humanReadableReleaseBranchList}"
    return humanReadableReleaseBranchList
}

def chequearSiExisteRama(String rama){
    def existe = false
    if ("${listarRamas()}".contains("${rama}"))
        existe = true
    /* este comando nos daba error de permisos
    try {
        def output = sh (script: "git ls-remote --heads ${rama}", returnStdout: true)
        if (output?.trim()) {
		    existe = true;
	    } 
    } catch (Exception a){
        existe = false;

    }*/

	return existe

}

def crearMerge(String releaseBranch, String targetBranch){
    try {
        sh '''
            git pull origin '''+releaseBranch+'''
            git checkout '''+targetBranch+'''
            git fetch -p
            git merge '''+releaseBranch+'''
            git push origin '''+targetBranch+'''
        '''
    } catch (Exception a){ } 
}