package com.vone.vone.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.klaytn.caver.Caver;
import com.klaytn.caver.abi.datatypes.Type;
import com.klaytn.caver.contract.Contract;
import org.springframework.beans.factory.annotation.Value;

import com.vone.vone.data.dao.VCDAO;
import com.vone.vone.data.entity.VC;
import com.vone.vone.service.KlaytnService;
import okhttp3.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.web3j.protocol.http.HttpService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class KlaytnServiceImpl implements KlaytnService {
    // You can directly input values for the variables below, or you can enter values in the caver-java-examples/.env file.
    @Value("${klaytn.nodeurl}")
    private String nodeApiUrl; // e.g. "https://node-api.klaytnapi.com/v1/klaytn";
    @Value("${klaytn.acesskey}")
    private String accessKeyId = ""; // e.g. "KASK1LVNO498YT6KJQFUPY8S";
    @Value("${klaytn.secretaccesskey}")
    private String secretAccessKey = ""; // e.g. "aP/reVYHXqjw3EtQrMuJP4A3/hOb69TjnBT3ePKG";
    @Value("${klaytn.chainid}")
    private String chainId = ""; // e.g. "1001" or "8217";
    @Value("${klaytn.contractaddress}")
    private String contractAddress = "";
    @Value("${klaytn.hashaddress}")
    private String hashAddress = "";

    @Value("${klaytn.salt}")
    private String salt = "";
    private final VCDAO vcDAO;

    @Autowired
    public KlaytnServiceImpl(VCDAO vcDAO){
        this.vcDAO = vcDAO;
    }

    public static String objectToString(Object value) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(value);
    }

    public String getVCFromKlaytn(String didId, Long vcId) throws Exception {
        HttpService httpService = new HttpService(nodeApiUrl);
        httpService.addHeader("Authorization", Credentials.basic(accessKeyId, secretAccessKey, StandardCharsets.UTF_8));
        httpService.addHeader("x-chain-id", chainId);
        Caver caver = new Caver(httpService);

        String abi = "[\n" +
                "{\n" +
                "\t\t\"constant\": true,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"did\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"vcId\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"getVCById\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"hashed\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t}"+
                "]";

        Contract contract = caver.contract.create(abi, contractAddress);
        List<Type> callResult = contract.call("getVCById", didId,vcId); //"did:vone:01E5B053B7ba8A91Bdb8FedD5814296c41aD522E"
        String res = objectToString(callResult.get(0));
        JSONObject jObject = new JSONObject(res);
        String response = (jObject.getJSONArray("value")).getJSONObject(1).getString("value");
        return response;
    }
    @Override
    public boolean verify(Long vcId) throws Exception{
        VC vc = vcDAO.selectVC(vcId);
        String didId = "did:vone:"+vc.getIssuer().substring(2);

        String hashOnChain = getVCFromKlaytn(didId, vcId);

        List<String> credentialSubject = new ArrayList<>(vc.getCredentialSubject().values());

        String hash = hash(credentialSubject);

        if(hash.equals(hashOnChain)){
            return true;
        }
        return false;
    }

    @Override
    public String hash(List<String> values) throws Exception {
       
        HttpService httpService = new HttpService(nodeApiUrl);
        httpService.addHeader("Authorization", Credentials.basic(accessKeyId, secretAccessKey, StandardCharsets.UTF_8));
        httpService.addHeader("x-chain-id", chainId);
        System.out.println(nodeApiUrl);
        Caver caver = new Caver(httpService);

        String abi = "[\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"string[]\",\n" +
                "\t\t\t\t\"name\": \"input\",\n" +
                "\t\t\t\t\"type\": \"string[]\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"hash\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"bytes32\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"bytes32\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"pure\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t}\n" +
                "]";

        Contract contract = caver.contract.create(abi, hashAddress);

        List<Type> callResult = contract.call("hash", values);

        String res = objectToString(callResult.get(0));
        JSONObject jObject = new JSONObject(res);
        String result = jObject.getString("value");

        return result;
    }
}
