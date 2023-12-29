package top.hcode.hoj.utils;

import top.hcode.hoj.common.exception.MossException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Locale;
import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class Moss {
    /**
     * 客户端的内部阶段。描述了MOSS服务的实质性操作，例如，如果没有发送文件，则客户端不能向服务器请求结果。
     *
     * @see SocketClient#getCurrentStage()
     *
     */
    public enum Stage {
        DISCONNECTED, AWAITING_INITIALIZATION, AWAITING_LANGUAGE, AWAITING_FILES, AWAITING_QUERY, AWAITING_RESULTS,
        AWAITING_END;
    }

    private static final String MESSAGE_UNKNOWN_LANGUAGE = "MOSS Server does not recognize this programming language";
    private static final String DEFAULT_LANGUAGE = "cc";
    private static final int STARTING_SETID = 1;

    private Socket socket;
    private Stage currentStage = Stage.DISCONNECTED;

    private String server;
    private int port;
    private String userID;
    private String language;
    private int setID = STARTING_SETID;
    private long optM = 10;
    private int optD = 1;
    private int optX = 0;
    private long optN = 250;
    private String optC = "";
    private URL resultURL;
    private List<String> supportedLanguages = Arrays.asList("c", "cc", "java",
            "ml", "pascal", "ada", "lisp", "schema", "haskell", "fortran",
            "ascii", "vhdl", "perl", "matlab", "python", "mips", "prolog",
            "spice", "vb", "csharp", "modula2", "a8086", "javascript", "plsql");

    private OutputStream out;
    private BufferedReader in;

    /**
     * 使用默认的主机、端口和要上传源文件的编程语言构造套接字客户端。默认语言是cpp。
     */
    public Moss() {
        this.server = "moss.stanford.edu";
        this.port = 7690;
        this.language = DEFAULT_LANGUAGE;
    }

    /**
     * 通过设置服务器和端口构造套接字客户端。
     *
     * @param server
     *               MOSS服务器的主机名或IP地址
     * @param port
     *               MOSS服务器的端口
     */
    public Moss(String server, int port) {
        this();
        this.server = server;
        this.port = port;
    }

    /**
     * 通过设置服务器名称、端口以及源文件的编程语言构造套接字客户端。
     *
     * @param server
     *                 MOSS服务器的主机名或IP地址
     * @param port
     *                 MOSS服务器的端口
     * @param language
     *                 所有源文件的编程语言
     */
    public Moss(String server, int port, String language) {
        this(server, port);
        this.language = language;
    }

    /**
     * 优雅地关闭与MOSS服务器的连接。
     */
    public void close() {
        /*
         * 在这里不要检查阶段，这样客户端可以在任何情况下关闭连接。
         */
        try {
            sendCommand("end\n");
            out.close();
            in.close();
            socket.close();
        } catch (MossException e) {
        } catch (IOException e2) {
        } finally {
            currentStage = Stage.DISCONNECTED;
        }

    }

    /**
     * 连接到MOSS服务器并为通信设置输入和输出流。
     *
     * @throws UnknownHostException
     *                              如果主机解析失败
     * @throws IOException
     *                              如果设置输入流失败
     * @throws SecurityException
     *                              如果安全管理器不允许连接
     */
    public void connect() throws UnknownHostException, IOException,
            SecurityException {
        if (currentStage != Stage.DISCONNECTED) {
            throw new RuntimeException("Client is already connected");
        }
        socket = new Socket(this.server, this.port);
        socket.setKeepAlive(true);
        out = socket.getOutputStream();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                Charsets.US_ASCII));
        currentStage = Stage.AWAITING_INITIALIZATION;
    }

    /**
     * @return 客户端的当前内部阶段。
     */
    public Stage getCurrentStage() {
        return currentStage;
    }

    /**
     * 内部递增设置ID。不要手动递增，除非有充分理由。可能会在将来被删除。
     *
     * @return 在此递增之前的最后设置ID。
     */
    @Deprecated
    public int getIncSetID() {
        return setID++;
    }

    public String getLanguage() {
        return language;
    }

    public String getOptC() {
        return optC;
    }

    public int getOptD() {
        return optD;
    }

    public long getOptM() {
        return optM;
    }

    public long getOptN() {
        return optN;
    }

    public int getOptX() {
        return optX;
    }

    public int getPort() {
        return port;
    }

    /**
     * @return 指向MOSS网页服务器上的剽窃报告的URL。
     */
    public URL getResultURL() {
        return resultURL;
    }

    public String getServer() {
        return server;
    }

    public int getSetID() {
        return setID;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * @return a list of supported programming languages by the MOSS server.
     */
    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public String getUserID() {
        return userID;
    }

    /**
     * 读取服务器响应的单行。
     *
     * @return 读取的行
     * @throws IOException
     *                     如果无法从套接字读取
     */
    public String readFromServer() throws IOException {
        return in.readLine();
    }

    /**
     * 向服务器发送所有种类命令的通用函数。
     *
     * @param objects
     *                包含要发送到服务器的参数的对象数组
     * @return 实际发送到服务器的生成字符串
     * @throws MossException
     *                       如果服务器响应意外
     */
    public void run() throws MossException, IOException, UnknownHostException {
        connect();
        sendInitialization();
        sendLanguage();
    }

    /**
     * 向服务器发送各种命令的通用函数。
     *
     * @param objects 包含应发送到服务器的参数的对象数组
     * @return 实际发送到服务器的生成字符串
     * @throws MossException 如果服务器响应意外
     */
    private String sendCommand(Object... objects) throws MossException {
        // TODO test
        Vector<String> commandStrings = new Vector<String>();
        String[] commandArray = new String[commandStrings.size()];
        for (Object o : objects) {
            String s;
            s = o.toString();

            commandStrings.add(s);
        }
        return sendCommandStrings(commandStrings.toArray(commandArray));
    }

    /**
     * 向服务器发送各种命令的通用函数。
     *
     * @param strings 包含应发送到服务器的参数的字符串数组
     * @return 实际发送到服务器的生成字符串
     * @throws MossException
     */
    private String sendCommandStrings(String... strings) throws MossException {
        if (strings == null || strings.length == 0) {
            throw new MossException(
                    "Failed to send command because it was empty.");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            sb.append(s);
            if (i != strings.length - 1) {
                sb.append(" ");
            }
        }
        sb.append('\n');
        try {
            byte[] bytes = (sb.toString()).getBytes(Charsets.US_ASCII);
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            throw new MossException("Failed to send command: " + e.getMessage());
        }

        return sb.toString();
    }

    /**
     * 向服务器发送初始化命令。在连接到服务器后但在发送任何文件之前需要调用此方法。
     *
     * @throws MossException
     */
    public void sendInitialization() throws MossException {
        if (currentStage != Stage.AWAITING_INITIALIZATION) {
            throw new RuntimeException(
                    "Cannot send initialization. Client is either already initialized or not connected yet.");
        }
        sendCommand("moss", userID);
        sendCommand("directory", optD);
        sendCommand("X", optX);
        sendCommand("maxmatches", optM);
        sendCommand("show", optN);
        currentStage = Stage.AWAITING_LANGUAGE;
    }

    // 此客户端目前仅使用目录模式。
    // public void setOptD(int optD) {
    // this.optD = optD;
    // }

    /**
     * 向服务器发送命令以定义所有源文件的编程语言。必须使用setLanguage()在服务器端之前设置语言。
     *
     * @see it.zielke.moji.SocketClient#setLanguage(String)
     * @throws MossException 如果MOSS服务器不支持此语言
     * @throws IOException   如果读取服务器响应时发生错误
     */
    public void sendLanguage() throws MossException, IOException {
        if (currentStage != Stage.AWAITING_LANGUAGE) {
            throw new RuntimeException(
                    "Language already sent or client is not initialized yet.");
        }
        sendCommand("language", language);
        // confirm valid language server-side
        String serverString;
        serverString = readFromServer();
        if (serverString == null
                || !serverString.trim().toLowerCase(Locale.ENGLISH)
                        .equals("yes")) {
            throw new MossException(MESSAGE_UNKNOWN_LANGUAGE);
        }
        currentStage = Stage.AWAITING_FILES;
    }

    /**
     * 向服务器发送命令以定义所有源文件的编程语言。
     *
     * @param language 所有源文件的编程语言
     * @throws MossException 如果MOSS服务器不支持此语言
     * @throws IOException   如果读取服务器响应时发生错误
     */
    public void sendLanguage(String language) throws MossException, IOException {
        setLanguage(language);
        sendLanguage();
    }

    /**
     * 向服务器发送命令告诉服务器所有文件都已上传，可以开始查找抄袭。等待服务器的响应，可能需要几分钟。
     *
     * @throws MossException 如果从MOSS服务器接收结果时发生错误
     * @throws IOException   如果服务器通信失败
     */
    public void sendQuery() throws MossException, IOException {
        if (currentStage != Stage.AWAITING_QUERY) {
            throw new RuntimeException(
                    "Cannot send query at this time. Connection is either not initialized or already closed");
        }
        if (setID == 1) {
            throw new MossException("You did not upload any files yet");
        }
        sendCommand(String.format(Locale.ENGLISH, "%s %d %s", "query", 0, optC));
        currentStage = Stage.AWAITING_RESULTS;
        // Query submitted, waiting for server's response
        String result = readFromServer();
        if (null != result
                && result.toLowerCase(Locale.ENGLISH).startsWith("http")) {
            try {
                this.resultURL = new URL(result.trim());
            } catch (MalformedURLException e) {
                throw new MossException(
                        "MOSS submission failed. The server did not return a valid URL with detection results.",
                        e);
            }
            currentStage = Stage.AWAITING_END;
        } else {
            throw new MossException(
                    "MOSS submission failed. The server did not return a valid URL with detection results.");
        }
    }

    /**
     * 设置所有源文件的编程语言。
     *
     * @param language 源文件编写的编程语言。有效的编程语言有：c、cc、java、ml、pascal、ada、
     *                 lisp、schema、haskell、fortran、ascii、vhdl、perl、matlab、
     *                 python、mips、prolog、spice、vb、csharp、modula2、a8086、
     *                 javascript 和 plsql。
     */
    public void setLanguage(String language) throws MossException {
        if (!supportedLanguages.contains(language)) {
            throw new MossException(MESSAGE_UNKNOWN_LANGUAGE);
        }
        this.language = language;
    }

    /**
     * 设置附加到MOSS报告的注释字符串。然后可以轻松区分各个报告。
     *
     * @param optC 注释字符串
     */
    public void setOptC(String optC) {
        this.optC = optC;
    }

    /**
     * 设置源代码段可能在被忽略并视为基本代码之前出现的最大次数。这是在上传基本
     * 模板代码时检测学生解决方案中的基本/模板代码使用的一种好方法。对应于原始客户端的-m参数。
     *
     * @param optM 出现次数超过后将被忽略的段数
     */
    public void setOptM(long optM) {
        this.optM = optM;
    }

    /**
     * 设置在MOSS结果中显示多少匹配项。默认值为250。
     *
     * @param optN 要显示的匹配项数量
     */
    public void setOptN(long optN) {
        this.optN = optN;
    }

    /**
     * 将此值设置为1会启用实验性的MOSS服务器。这基本上是未经测试的。默认值为0。
     *
     * @param optX 0或1。0：常规MOSS服务器，1：实验性MOSS服务器。
     */
    public void setOptX(int optX) {
        this.optX = optX;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * 为与MOSS服务器进行身份验证设置MOSS用户ID。
     *
     * @param userID 用于进行身份验证的个性化ID。应该类似于/[0-9]+/，在位于
     *               http://theory.stanford.edu/~aiken/moss/ 的MOSS页面上可用。
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * 将单个文件上传到MOSS服务器。
     *
     * @param file 要上传的源代码文件
     * @throws IOException 如果无法读取文件
     */
    public void uploadFile(File file) throws IOException {
        uploadFile(file, false);
    }

    /**
     * 将单个字符串代码上传到MOSS服务器。
     *
     * @param code 要上传的源代码
     * @throws IOException 如果代码为空
     */
    public void uploadString(String filename, String code) throws IOException {
        uploadString(filename, code, false);
    }

    /**
     * 将单个基本文件上传到MOSS服务器。
     *
     * @param file 要上传的基本文件
     * @throws IOException 如果无法读取文件
     */
    public void uploadBaseFile(File file) throws IOException {
        uploadFile(file, true);
    }

    /**
     * 将单个文件上传到MOSS服务器。
     *
     * @param file       要上传的源代码文件
     * @param isBaseFile true表示基本文件，否则为false。
     * @throws IOException 如果无法读取文件
     */
    public void uploadFile(File file, boolean isBaseFile) throws IOException {
        if (currentStage != Stage.AWAITING_FILES
                && currentStage != Stage.AWAITING_QUERY) {
            throw new RuntimeException(
                    "Cannot upload file. Client is either not initialized properly or the connection is already closed");
        }
        byte[] fileBytes = FileUtils.readFileToByteArray(file);
        String filename = normalizeFilename(file.getAbsolutePath());
        String uploadString = String.format(Locale.ENGLISH,
                "file %d %s %d %s\n", // format:
                isBaseFile ? 0 : getIncSetID(), // 1. setID
                language, // 2. language
                fileBytes.length, // 3. size
                /*
                 * Use Unix-style path to remain consistent. TODO test this with
                 * non-local files, e.g. on network shares
                 */
                filename); // 4. file path
        System.out.println("uploading file: " + filename);
        out.write(uploadString.getBytes(Charsets.UTF_8));
        out.write(fileBytes);

        currentStage = Stage.AWAITING_QUERY;

    }

    public String normalizeFilename(String filename) {
        String result = Normalizer.normalize(filename, Normalizer.Form.NFD);
        result = FilenameUtils.normalizeNoEndSeparator(result, true);
        result = result.replace(" ", "$"); // 将空格替换为 '$'，或者其他你希望的ASCII字符
        return result;
    }

    /**
     * 将单个字符串上传到MOSS服务器。
     *
     * @param code         要上传的代码
     * @param isBaseString 如果为true，则为基本字符串；否则为false。
     * @throws IOException 如果上传字符串时发生错误
     */
    public void uploadString(String filename, String code, boolean isBaseString) throws IOException {
        if (currentStage != Stage.AWAITING_FILES
                && currentStage != Stage.AWAITING_QUERY) {
            throw new RuntimeException(
                    "Cannot upload string. Client is either not initialized properly or the connection is already closed");
        }

        filename = normalizeFilename(filename);

        byte[] fileBytes = code.getBytes(Charsets.UTF_8);

        String uploadString = String.format(Locale.ENGLISH,
                "file %d %s %d %s \n", // format:
                isBaseString ? 0 : getIncSetID(), // 1. setID
                language, // 2. language
                fileBytes.length, // 3. size
                /*
                 * Use Unix-style path to remain consistent. TODO test this with
                 * non-local files, e.g. on network shares
                 */
                filename); // 4. file path

        // System.out.println("uploading string: " + uploadString);
        out.write(uploadString.getBytes(Charsets.UTF_8));
        out.write(fileBytes); // Assuming UTF-8 encoding for strings

        currentStage = Stage.AWAITING_QUERY;
    }
}
