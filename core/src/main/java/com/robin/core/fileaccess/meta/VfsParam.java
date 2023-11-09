package com.robin.core.fileaccess.meta;

import com.robin.core.base.util.Const;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class VfsParam {
    private String protocol;
    private String hostName;
    private int port;
    private String userName;
    private String password;

    private boolean lockDir = false;
    private boolean passive = false;
    private String proxyHost;
    private int proxyPort;
    private String proxySchema;
    private String keystoreFiles;
    private String keystorePass;
    private String keystoreType;

    public VfsParam() {

    }

    public VfsParam(String hostName, String protocol, String userName, String password) {
        this.hostName = hostName;
        this.protocol = protocol;
        adjustProtocol();
        this.userName = userName;
        this.password = password;
    }

    public VfsParam(String hostName, String protocol, int port, String userName, String password) {
        this.hostName = hostName;
        this.protocol = protocol;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public void adjustProtocol() {
        if (Const.VFS_PROTOCOL.FTP.getValue().equalsIgnoreCase(this.protocol)) {
            port = 21;
        } else if (Const.VFS_PROTOCOL.SFTP.getValue().equalsIgnoreCase(this.protocol)) {
            port = 22;
        } else if (Const.VFS_PROTOCOL.HTTP.getValue().equalsIgnoreCase(this.protocol)) {
            port = 80;
        } else if (Const.VFS_PROTOCOL.HTTPS.getValue().equalsIgnoreCase(this.protocol)) {
            port = 445;
        }
    }
}