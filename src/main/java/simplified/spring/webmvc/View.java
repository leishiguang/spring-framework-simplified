package simplified.spring.webmvc;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义的模板解析引擎
 *
 * @author leishiguang
 * @since v1.0
 */
@AllArgsConstructor
@Slf4j
public class View {

	private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf-8";

	private File viewFile;

	public String getContentType() {
		return DEFAULT_CONTENT_TYPE;
	}

	public void render(Map<String, ?> model, HttpServletRequest req, HttpServletResponse resp) {
		StringBuffer sb = new StringBuffer();
		RandomAccessFile ra = null;
		try {
			ra = new RandomAccessFile(this.viewFile, "r");
		} catch (FileNotFoundException e) {
			log.error("读取文件错误", e);
		}
		String line = null;
		assert ra != null;
		while (true) {
			try {
				line = ra.readLine();
			} catch (IOException e) {
				log.error("readLine错误", e);
			}
			if (line == null) {
				break;
			}
			line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
			String reg = "￥\\{[^}]+}";
			Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				String paramName = matcher.group().replaceAll("￥\\{|}", "");
				Object paramValue = model.get(paramName);
				if (paramValue == null) {
					continue;
				}
				//要把￥{}中间的这个字符串取出来
				line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
				matcher = pattern.matcher(line);
			}
			sb.append(line);
		}
		try {
			ra.close();
		} catch (IOException e) {
			log.error("关闭 ra 失败",e);
		}
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		try {
			resp.getWriter().write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String makeStringForRegExp(String str) {
		return str.replace("\\","\\\\")
				.replace("*","\\*")
				.replace("+","\\+");
	}

}
