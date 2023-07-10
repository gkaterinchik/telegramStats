import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class WordStats {
    ObjectMapper objectMapper;
    List<Message> messageList;
    String filePath;
    Map<String, Integer> sortedMap;
    String name;
    File file;
    Map<String, Integer> members;


    public WordStats(String filePath){
        this.filePath=filePath;
        this.objectMapper=new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.file=new File(filePath);
    }

    public WordStats(String filePath, String name) {
        this.filePath = filePath;
        this.name = name;
        this.objectMapper=new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.file=new File(filePath);

    }

    public WordStats() {
        this.objectMapper=new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public void getDialogueMembers() throws IOException {
        this.file=new File(filePath);
        List<Member>members;
        List<String>tmp=new ArrayList<>();
        extractMessagesFromFile(file);

        for (Message m:messageList) {
           tmp.add(m.getFrom());

        }
        this.members=calculateRepeats(tmp);


    }

    private  <T> Map<T, Integer> calculateRepeats(List<T> inputList) {
        Map<T, Integer> resultMap = new HashMap<>();
        inputList.forEach(e -> resultMap.compute(e, (k, v) -> Math.toIntExact(v == null ? 1L : v + 1L)));
        return resultMap;
    }
    public void extractMessagesFromFile(File file) throws IOException {
        Discussion disc=objectMapper.readValue(file,new TypeReference<>() {});
        System.out.println(disc.messages.size());
        messageList=new ArrayList<>();
        for (Object me:disc.messages) {
           String json= objectMapper.writeValueAsString(me);
           messageList.add(objectMapper.readValue(json.getBytes(StandardCharsets.UTF_8), Message.class));

       }

    }
   // private void extractMessagesFromFile(File file) throws IOException {
     //   this.messageList=objectMapper.readValue(file,new TypeReference<>() {});
    //}
    //на выходе получаем список объектов вида: MessageEntity{type='plain', text='решилось', from='Григорий Катеринчик'}
    private List<MessageEntity> createMessageEntityList(){
        List<MessageEntity> messageEntities = new ArrayList<>();
        MessageEntity tempEntity;
        for (Message mes : messageList) {
            for (Object entity : mes.getText_entities()) {
                LinkedHashMap<String, String> linkedHashMap;

                linkedHashMap = (LinkedHashMap<String, String>) entity;
                tempEntity = new MessageEntity();
                tempEntity.setType(linkedHashMap.get("type"));
                tempEntity.setText(linkedHashMap.get("text"));
                tempEntity.setFrom(mes.getFrom());

                messageEntities.add(tempEntity);
            }

        }
        return messageEntities;
    }

    //Получаем отформатированный список всех слов из всех сообщений
    private List<String> createFormattedMessageList(List<MessageEntity> messageEntitiesList){
        List<String> meslist = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<String> allMessage=new ArrayList<>();

        for (MessageEntity me : messageEntitiesList) {
            if(name==null) {
                if (me.getType().equals("plain"))
                    meslist.add(me.getText());
            }
            if(name!=null) {
                if (me.getType().equals("plain") && me.getFrom().equals(name))
                    meslist.add(me.getText());
            }

        }

        String formattedStr;
        for (String s : meslist) {

            formattedStr = s;
            formattedStr = formattedStr
                    .toLowerCase()
                    .replace("(", "")
                    .replace(")", "")
                    .replace(",", "")
                    .replace("?", "")
                    .replace("!", "")
                    .replace(".", "")
                    .replace(":", "")
            ;

            messages.add(formattedStr);

        }
        System.out.println(messages.size());
        for (String m : messages) {
            allMessage.addAll(List.of(m.split(" ")));
        }
        return allMessage;
    }

    private Map<String, Integer> createResultSortedMap(Map<String, Integer> resultMap){
       sortedMap=resultMap.entrySet().stream().sorted(Comparator.comparingInt(e -> -e.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
            throw new AssertionError();
        }, LinkedHashMap::new));

        return sortedMap;

    }
    private void createResultFile(Map<String, Integer> sortedMap,String fileName){

        try(FileWriter writer = new FileWriter(fileName, true))
        {
            for (Map.Entry entry:sortedMap.entrySet()) {
                String str=entry.getKey()+" = "+entry.getValue();
                writer.write(str);
                writer.append('\n');
                writer.flush();

            }
            writer.write(((Integer)sortedMap.size()).toString());
            writer.append('\n');
            writer.flush();
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }
    public void run(String outputFileName) throws IOException {
        this.extractMessagesFromFile(this.file);
        this.createResultFile(this.createResultSortedMap(this.calculateRepeats(this.createFormattedMessageList(this.createMessageEntityList()))),outputFileName);

    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setName(String name) {
        this.name = name;
    }
}


