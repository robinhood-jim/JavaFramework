package com.robin.hadoop.security;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedExceptionAction;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.Closer;


public class ProxiedFileSystemWrapper {
	 private static final Logger LOG = LoggerFactory.getLogger(ProxiedFileSystemWrapper.class);
	public enum AuthType {
	    TOKEN,
	    KEYTAB;
	  }
	private FileSystem proxiedFs;
	public FileSystem getProxiedFileSystem(String superUserName,String proxyUserName,AuthType authType, String authPath,final Configuration conf, String uri)
		      throws IOException, InterruptedException, URISyntaxException {
		Preconditions.checkArgument(StringUtils.isNotBlank(superUserName),
		        "cluster does not contain a login user name");
		UserGroupInformation proxyUser;
		switch (authType) {
	      case KEYTAB: // If the authentication type is KEYTAB, log in a super user first before creating a proxy user.
	        Preconditions.checkArgument(
	            StringUtils.isNotBlank(proxyUserName),
	            "State does not contain a proper proxy token file name");
	        UserGroupInformation.loginUserFromKeytab(superUserName, authPath);
	        proxyUser = UserGroupInformation.createProxyUser(proxyUserName, UserGroupInformation.getLoginUser());
	        break;
	      case TOKEN: // If the authentication type is TOKEN, create a proxy user and then add the token to the user.
	        proxyUser = UserGroupInformation.createProxyUser(proxyUserName, UserGroupInformation.getLoginUser());
	        Optional<Token> proxyToken = getTokenFromSeqFile(authPath, proxyUserName);
	        if (proxyToken.isPresent()) {
	          proxyUser.addToken(proxyToken.get());
	        } else {
	          LOG.warn("No delegation token found for the current proxy user.");
	        }
	        break;
	      default:
	        LOG.warn("Creating a proxy user without authentication, which could not perform File system operations.");
	        proxyUser = UserGroupInformation.createProxyUser(proxyUserName, UserGroupInformation.getLoginUser());
	        break;
		}
		final URI fsURI = URI.create(uri);
	    proxyUser.doAs(new PrivilegedExceptionAction<Void>() {
	      @Override
	      public Void run() throws IOException {
	        LOG.debug("Now performing file system operations as :" + UserGroupInformation.getCurrentUser());
	        proxiedFs = FileSystem.get(fsURI, conf);
	        return null;
	      }
	    });
	    return this.proxiedFs;
	}
	private Optional<Token> getTokenFromSeqFile(String authPath, String proxyUserName) throws IOException {
	    Closer closer = Closer.create();
	    try {
	      FileSystem localFs = FileSystem.getLocal(new Configuration());
	      @SuppressWarnings("deprecation")
	      SequenceFile.Reader tokenReader =
	          closer.register(new SequenceFile.Reader(localFs, new Path(authPath), localFs.getConf()));
	      Text key = new Text();
	      Token value = new Token();
	      while (tokenReader.next(key, value)) {
	        LOG.info("Found token for " + key);
	        if (key.toString().equals(proxyUserName)) {
	          return Optional.of(value);
	        }
	      }
	    } catch (Throwable t) {
	      throw closer.rethrow(t);
	    } finally {
	      closer.close();
	    }
	    return Optional.absent();
	  }
}
