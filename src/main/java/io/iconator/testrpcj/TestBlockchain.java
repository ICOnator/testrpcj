package io.iconator.testrpcj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import io.iconator.testrpcj.jsonrpc.AddContentTypeFilter;
import io.iconator.testrpcj.jsonrpc.EthJsonRpcImpl;
import io.iconator.testrpcj.jsonrpc.JsonRpc;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.blockchain.EtherUtil;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.spongycastle.util.encoders.Hex;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class TestBlockchain {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestBlockchain.class);

    // public and private keys
    public final static ECKey ACCOUNT_0 = ECKey.fromPrivate(Hex.decode("1b865950b17a065c79b11ecb39650c377b4963d6387b2fb97d71744b89a7295e"));
    public final static ECKey ACCOUNT_1 = ECKey.fromPrivate(Hex.decode("c77ee832f3e5d7624ce9dab0eeb2958ad550e534952b79bb705e63b3989d4d1d"));
    public final static ECKey ACCOUNT_2 = ECKey.fromPrivate(Hex.decode("ba7ffe9dee14b3626211b2d056eacc30e7a634f7e11eeb4dde6ee6d50d0c81ab"));
    public final static ECKey ACCOUNT_3 = ECKey.fromPrivate(Hex.decode("64b1a16bb773bc2a6665967923cfd68f369e34f66ecd19c302995f8635598b1c"));
    public final static ECKey ACCOUNT_4 = ECKey.fromPrivate(Hex.decode("399c34e860be1f2740297fcadd3546fdd4f5ba4c06d13882da1e48527df3acca"));
    public final static ECKey ACCOUNT_5 = ECKey.fromPrivate(Hex.decode("a2a3abebd9160a2b2940970d848161008e3ea528aeaa927fb8b8370d3675f5f5"));
    public final static ECKey ACCOUNT_6 = ECKey.fromPrivate(Hex.decode("e728d9667a27b7f6164309fc3809c00fd8d782d9343c0b73ea1f5a150ec3d05b"));
    public final static ECKey ACCOUNT_7 = ECKey.fromPrivate(Hex.decode("d58fd771caefbdcca0c23fbc440fd03dacdee29cc4668cc9fc5acf29b4219f41"));
    public final static ECKey ACCOUNT_8 = ECKey.fromPrivate(Hex.decode("649f638d220fd6319ca4af8f5e0e261d15a66172830077126fef21fdbdd95410"));
    public final static ECKey ACCOUNT_9 = ECKey.fromPrivate(Hex.decode("ea8f71fc4690e0733f3478c3d8e53790988b9e51deabd10185364bc59c58fdba"));

    private final static Integer DEFAULT_PORT = 8585;

    private Server server = null;
    private StandaloneBlockchain standaloneBlockchain = null;

    public static void main(String[] args) throws Exception {
        Integer port = null;
        TestBlockchain t = new TestBlockchain();
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                LOG.info("The given parameter is not a number: {}", args[0]);
                port = DEFAULT_PORT;
            }
        }
        LOG.info("Using port: {}", port);
        t.start(port);
    }

    public static SolidityCompiler compiler() {
        return new SolidityCompiler(SystemProperties.getDefault());
    }

    public TestBlockchain start() throws Exception {
        return start(DEFAULT_PORT);
    }

    public TestBlockchain start(int port) throws Exception {
        if (server != null) {
            stop();
        }
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        standaloneBlockchain = new StandaloneBlockchain()
                .withAccountBalance(TestBlockchain.ACCOUNT_0.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_1.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_2.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_3.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_4.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_5.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_6.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_7.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_8.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAccountBalance(TestBlockchain.ACCOUNT_9.getAddress(), EtherUtil.convert(10, EtherUtil.Unit.ETHER))
                .withAutoblock(true);  //after each transaction, a new block will be created
        standaloneBlockchain.createBlock();
        EthJsonRpcImpl ethJsonRpcImpl = new EthJsonRpcImpl(standaloneBlockchain);

        JsonRpcServer rpcServer = new JsonRpcServer(new ObjectMapper(), ethJsonRpcImpl, JsonRpc.class);
        RPCServlet rpcServlet = new RPCServlet(rpcServer);
        ServletHolder holder = new ServletHolder(rpcServlet);
        context.addServlet(holder, "/rpc");
        context.addFilter(AddContentTypeFilter.class, "/rpc", EnumSet.of(DispatcherType.REQUEST));
        server.start();

        return this;
    }

    private TestBlockchain stop() throws Exception {
        server.stop();
        server.destroy();
        server = null;
        standaloneBlockchain = null;
        return this;
    }

}
