package dev.xing.infinitechat.momentservice.utlis;
@SuppressWarnings({"all"})
public class ContentTypeUtil {


    public String getType(String fileSuffix){
        String contentType = "application/octet-stream";  // 默认二进制文件类型
        switch (fileSuffix) {
            case "jpg":
            case "jpeg":
                contentType = "image/jpeg";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "gif":
                contentType = "image/gif";
                break;
            // 你可以根据实际需要继续添加其他图片格式
            default:
                contentType = "application/octet-stream"; // 默认的二进制流
                break;
        }
        return contentType;
    }
}
