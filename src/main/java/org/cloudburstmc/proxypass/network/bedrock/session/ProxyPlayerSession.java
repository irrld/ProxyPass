package org.cloudburstmc.proxypass.network.bedrock.session;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.cloudburstmc.protocol.bedrock.util.ChainValidationResult;
import org.cloudburstmc.proxypass.ProxyPass;
import org.cloudburstmc.proxypass.network.bedrock.logging.SessionLogger;

import java.nio.file.Path;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@Getter
public class ProxyPlayerSession {

    private final ProxyServerSession upstream;
    private final ProxyClientSession downstream;
    private final ProxyPass proxy;
    private final ChainValidationResult.IdentityData identityData;
    private final long timestamp = System.currentTimeMillis();

    @Getter(AccessLevel.PACKAGE)
    private final KeyPair proxyKeyPair;
    private final Path dataPath;
    private final PackDownloader packDownloader;
    private final List<String> packIds = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private final AtomicBoolean closed = new AtomicBoolean();

    public final SessionLogger logger;

    public ProxyPlayerSession(ProxyServerSession upstream, ProxyClientSession downstream, ProxyPass proxy, ChainValidationResult.IdentityData identityData, KeyPair keyPair) {
        this.upstream = upstream;
        this.downstream = downstream;
        this.proxy = proxy;
        this.identityData = identityData;
        this.proxyKeyPair = keyPair;
        this.dataPath = proxy.getSessionsDir().resolve(identityData.displayName + '-' + timestamp);
        this.packDownloader = new PackDownloader(this.dataPath);
        this.logger = new SessionLogger(
                proxy,
                this.dataPath
        );
        logger.start();
    }

    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            this.upstream.getPeer().getChannel().close();
            this.downstream.getPeer().getChannel().close();
            this.packDownloader.close();
            this.logger.close();
        }
    }
}
