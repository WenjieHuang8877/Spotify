package com.laioffer

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class Song(
    val name: String,
    val lyric: String,
    val src: String,
    val length: String
)

@Serializable
data class Playlist(
    val id: Long,

    val songs: List<Song>
)


fun main() {
    // 在主函数中，启动服务器，配置为Netty，端口为8080，主机为0.0.0.0，并加载Application的模块
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)

//    // 调用一个函数noName，参数为5和一个lambda函数
//    noName(5) { y: Int ->
//        true
//    }
}



// 一个接受一个整型参数和一个函数参数的函数noName，函数参数接受一个整型输入并返回一个布尔值
fun noName(x: Int, y: (Int) -> Boolean) {}

// Application的扩展函数module，用于配置服务器的功能
fun Application.module() {

    // 安装ContentNegotiation，配置为json，并设置json输出为prettyPrint模式
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }

    // 配置路由
    routing {
        // 在根路径（"/"）上配置GET方法，响应"Hello World!"文本
        get("/") {
            call.respondText("Hello World!")
        }

        // 在"/feed"路径上配置GET方法，读取资源中的"feed.json"文件，并将其作为json响应
        get("/feed") {
            val jsonString = this::class.java.classLoader.getResource("feed.json").readText()
            val json = Json.parseToJsonElement(jsonString)
            call.respondText(json.toString(), ContentType.Application.Json)
        }

        // 配置"/playlists"路径的GET请求处理
        get("/playlists") {
            // 从服务器的资源目录中加载名为"playlist.json"的文件，读取其全部文本内容到jsonString变量中
            val jsonString = this::class.java.classLoader.getResource("playlist.json").readText()

            // 使用Kotlinx的Json库将jsonString解析成JsonElement对象
            val json = Json.parseToJsonElement(jsonString)

            // 将解析得到的JsonElement对象转换成字符串，并作为响应发送给请求者，响应的内容类型设定为Json
            call.respondText(json.toString(), ContentType.Application.Json)
        }


        // 配置静态资源的访问路径
        static("/") {
            // 配置静态资源的基础包路径，这里设置为"static"，即静态资源存放在resources/static文件夹下
            staticBasePackage = "static"

            // 在静态资源中，配置一个名为"songs"的子路径，可以通过"/songs"访问到此路径下的资源
            static ("songs"){
                // 这里表示在"songs"路径下，将会提供resources/static/songs文件夹下的资源
                resources("songs")
            }
        }
        // 配置"/playlist/{id}"路径的GET请求处理
        get("/playlist/{id}") {
            // 从服务器的资源目录中加载名为"playlists.json"的文件，读取其全部文本内容到jsonString变量中
            val jsonString = this::class.java.classLoader.getResource("playlists.json").readText()

            // 使用Kotlinx的Json库将jsonString解析成Playlist对象列表
            // 告诉了decoder首先是个list，然后list里面是playlist
            val playlists: List<Playlist> = Json.decodeFromString(ListSerializer(Playlist.serializer()), jsonString)

            // 从请求路径中获取{id}参数，存储在id变量中
            val id: String? = call.parameters["id"]

            // 在解析得到的Playlist对象列表中查找id与路径中{id}参数相符的对象
            // 使用firstOrNull函数，如果找不到对应的对象，将返回null
            val playlist: Playlist? = playlists.firstOrNull { it.id.toString() == id }

            // 将查找结果返回给请求者，如果playlist为null，也即没有找到对应的对象，将返回空响应
            call.respondNullable(playlist)
        }

    }
}
