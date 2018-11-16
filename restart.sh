#!/usr/bin/env bash

function compile()
{
	echo "rebuild project"
	mvn clean package
}

function hasCodeUpdate()
{
	git fetch origin master
	diff=`git diff FETCH_HEAD`
	if [ ! -n "$diff" ] ;then
            return 0
	fi
	    echo "服务器代码更新"
		return 1
}

function pullNewCode()
{
    git fetch origin master
    git merge
    echo "拉取新代码完成"

}

function checkServer()
{
	toy_proxy=`ps -ef | grep "toy-proxy" | grep "java" | grep -v "grep"`
	if [ -n "${toy_proxy}" ] ;then
        	echo "服务正常启动"
        	echo "${toy_proxy}"
		return 0;
	fi

	#这句代码有问题,判断不生效
	if [ ! -f "target/toy-proxy.jar" ] ;then
		echo "项目执行文件不存在，重新编译"
		compile
	fi

	echo "start server"

    nohup java -jar ./target/toy-proxy.jar &


    toy_proxy=`ps -ef | grep "toy-proxy" | grep "java" | grep -v "grep"`
    if [ -n "$toy_proxy" ] ;then
           echo "server start success"
           echo "$toy_proxy"
                return 0
    else
            echo "new server start failed please check IT!!!"
            return 1
    fi
}

function killServer(){
    toy_proxy=`ps -ef | grep "toy-proxy" | grep "java"  | grep -v "grep"`
    if [ -n "$toy_proxy" ] ;then
    	echo "kill old server process"
    	ps -ef | grep "toy-proxy" | grep -v "grep" | awk '{print $2}' | xargs kill -9
    	echo `ps -ef | grep "toy-proxy" | grep -v "grep"`

    fi
}


hasCodeUpdate
updateStatus=$?
if [ ! ${updateStatus} -eq 0 ] ;then
    killServer
    echo "clean code"
    git clean -dfx
    echo "pull new code"
    git pull
#    pullNewCode
    compile
fi
checkServer

#tail -f nohup.out