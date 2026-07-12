package org.fjnu305.acm01.tools;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * 本地运行，将 QQ 邮箱 SMTP 授权码加密为 ENC(...) 密文。
 * <p>
 * 用法（任选其一）：
 * <pre>
 *   # 方式 A：当前终端先设环境变量（与启动后端时用同一个主密码）
 *   $env:JASYPT_ENCRYPTOR_PASSWORD="主密码"
 *   mvn -q compile exec:java "-Dexec.mainClass=org.fjnu305.acm01.tools.JasyptEncryptTool" "-Dexec.args=16位授权码"
 *
 *   # 方式 B：一条命令传主密码（仅本机临时用，不要提交 Git）
 *   mvn -q compile exec:java "-Dexec.mainClass=org.fjnu305.acm01.tools.JasyptEncryptTool" "-Dexec.args=16位授权码" "-Djasypt.encryptor.password=主密码"
 * </pre>
 * </p>
 */
public final class JasyptEncryptTool {

    private JasyptEncryptTool() {
    }

    public static void main(String[] args) {
        if (args.length == 0 || args[0].isBlank()) {
            printUsage();
            System.exit(1);
        }

        String masterPassword = resolveMasterPassword();
        if (masterPassword == null || masterPassword.isBlank()) {
            System.err.println("未设置主密码，无法加密。请任选一种方式：");
            System.err.println("  1) 当前 PowerShell: $env:JASYPT_ENCRYPTOR_PASSWORD=\"主密码\"");
            System.err.println("  2) mvn 参数: -Djasypt.encryptor.password=主密码");
            System.err.println("注意：在 Windows「系统环境变量」或 IDEA Run 里设的变量，当前终端可能读不到，需重开终端或用方式 2。");
            System.exit(1);
        }

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(masterPassword);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setPoolSize(1);
        config.setKeyObtentionIterations("1000");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);

        String plain = args[0].trim();
        String encrypted = encryptor.encrypt(plain);
        System.out.println("ENC(" + encrypted + ")");
        System.out.println("请把上面一行填入 application-local.yml 的 spring.mail.password");
    }

    private static String resolveMasterPassword() {
        String fromEnv = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProperty = System.getProperty("jasypt.encryptor.password");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        return null;
    }

    private static void printUsage() {
        System.err.println("用法: JasyptEncryptTool <明文授权码>");
        System.err.println("主密码来源: 环境变量 JASYPT_ENCRYPTOR_PASSWORD 或 -Djasypt.encryptor.password");
    }
}
