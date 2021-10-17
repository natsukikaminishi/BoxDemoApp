package com.example.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxUser;

/***
 * デモコントローラー
 */
@Controller
public class DemoApplicationController {

	public static String configPath = "D:\\Box\\pleiades\\config.json";
	
    /**
     * 初期処理
     * @return
     */
    @GetMapping
    String index(Model model) {
    	
        model.addAttribute("demoForm", new DemoForm());

        return "/index";
    }

    /**
     * ファイルアップロード処理
     * @param uploadForm
     * @return
     * @throws IOException 
     */
    @PostMapping("/upload")
    String upload(DemoForm demoForm, Model model){
        //アップロード実行処理メソッド呼び出し
		try {
			uploadAction(demoForm);
		} catch (Exception e) {
			model.addAttribute("message", e.getMessage());
		}
    	 return "/index";
    }
    
    /**
     * ユーザー作成処理
     * @param demoForm
     * @param model
     * @return
     * @throws IOException 
     */
	@PostMapping("/createUser")
	String userCreate(DemoForm demoForm, Model model){
		try {
			// 認証情報取得
			Reader reader = new FileReader(configPath);
			BoxConfig config = BoxConfig.readFrom(reader);
			// APIConnection取得
			BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config);
			// CreateAppUserAPI実行
			BoxUser.createAppUser(api, demoForm.getUserName());
		} catch (Exception e) {
			// エラーの場合は、メッセージ表示
			model.addAttribute("message", e.getMessage());
		}
		return "/index";
	}

    /**
     * アップロード実行処理
     * @param demoForm
     * @throws IOException 
     */
    private void uploadAction(DemoForm demoForm) throws IOException {
		// アップロードファイル取得
		MultipartFile multipartFile = demoForm.getMultipartFile();
		File file = multipartToFile(multipartFile, multipartFile.getOriginalFilename());

		// 認証情報取得
		Reader reader = new FileReader(configPath);
		BoxConfig config = BoxConfig.readFrom(reader);
		// AuthOprionによる分岐処理
		BoxDeveloperEditionAPIConnection api = null;

		if (demoForm.getAuthOption().equals("1")) {
			// EnterpriseでのConnection
			api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config);
		} else if (demoForm.getAuthOption().equals("2")) {
			// EnterpriseでのConnection + as userヘッダーによる成り代わり
			api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config);
			api.asUser(demoForm.getUserId());
		} else if (demoForm.getAuthOption().equals("3")) {
			// ユーザー指定のアクセストークンでのConnection
			api = BoxDeveloperEditionAPIConnection.getAppUserConnection(demoForm.getUserId(), config);
		} else {
			// 何もしない
		}

		// フォルダID指定
		BoxFolder folder = new BoxFolder(api, demoForm.getFolderId());

		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			folder.uploadFile(stream, multipartFile.getOriginalFilename());
		} catch (Exception e) {
			throw e;
		} finally {
			stream.close();
			file.delete();
		}

	}
    
    private File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
        multipart.transferTo(convFile);
        return convFile;
    }
    
    
}
