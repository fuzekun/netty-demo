package transport;


import io.netty.handler.codec.http.multipart.FileUpload;
import org.json.simple.JSONObject;


import java.util.Map;

/**
 * <p>请求对象</p>
 *
 * @author fuzekun
 */
public class MultipartRequest {
    // 文件名： 文件上传句柄
    private Map<String, FileUpload> fileUploads;
    private JSONObject params;

    public Map<String, FileUpload> getFileUploads() {
        return fileUploads;
    }

    public void setFileUploads(Map<String, FileUpload> fileUploads) {
        this.fileUploads = fileUploads;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }
}