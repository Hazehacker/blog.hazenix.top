import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const request = axios.create({
    baseURL: "http://localhost:9090",
    timeout: 30000 //后台接口超时时间

    })

//request 拦截器
//可以自动在请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    return config
},error => {
    return Promise.reject(error)
});

//response 拦截器
//可以自动在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        //兼容服务器返回的字符串数据
        if(typeof res === 'string'){
            res = res ? JSON.parse(res):res;
        }
        return res;
    },
    error => {
        if(error.response){
            if(error.response.status === 404) {
                ElMessage.error('未找到请求接口 ')
            }else if(error.response.status === 500){
                ElMessage.error('系统异常，请查看后端控制台报错')
            }else {
                console.error(error.message)
            }
        }else{
            //处理网络请求失败的情况：
            ElMessage.error('网络请求失败，请检查网络连接');

        }
        return Promise.reject(error)
    }

)


export default request

