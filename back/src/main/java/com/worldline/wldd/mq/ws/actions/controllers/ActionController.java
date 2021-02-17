package com.worldline.wldd.mq.ws.actions.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.worldline.wldd.mq.ws.actions.entities.Action;
import com.worldline.wldd.mq.ws.demonstrations.entities.Demo;

import com.worldline.wldd.mq.ws.actions.repositories.ActionRepository;
import com.worldline.wldd.mq.ws.demonstrations.repositories.DemoRepository;
import com.worldline.wldd.mq.ws.parameters.entities.Parameters;
import com.worldline.wldd.mq.ws.parameters.repositories.ParametersRepository;
import com.worldline.wldd.mq.ws.tools.FormatedMessage;

import com.worldline.wldd.mq.ws.users.entities.UserGroups;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




@Controller    // This means that this class is a Controller
@RequestMapping(path="action") // This means URL's start with /action (after Application path)
public class ActionController {
    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private ActionRepository actionRepository;

    @Autowired
    public DemoRepository demoRepository;

    @Autowired
    public ParametersRepository parametersRepository;

    private static final String MESSAGE = "MESSAGE";
    private static final String TOPIC = "TOPIC";

    @PostMapping(path="/addAction") // Map ONLY GET Requests
    public @ResponseBody
    String addNewAction (@RequestParam Integer demoId, @RequestParam String name, @RequestParam String topic, @RequestParam String message, @RequestParam Integer adminId) {
        Optional<Demo> demo = demoRepository.findById(demoId);
        if(demo.isPresent())
        {
            int i;
            List<String> paramsTopic = fillNewParameters(topic);
            List<Parameters> listParamsTopic = new ArrayList<Parameters>();
            for(i=0;i<paramsTopic.size();i++){
                Parameters tempParametersTopic = new Parameters();
                tempParametersTopic.setParameter(paramsTopic.get(i));
                tempParametersTopic.setType(TOPIC);
                tempParametersTopic = parametersRepository.save(tempParametersTopic);
                listParamsTopic.add(tempParametersTopic);
            }

            List<String> paramsMessage = fillNewParameters(message);
            List<Parameters> listParamsMessage = new ArrayList<Parameters>();
            for(i=0;i<paramsMessage.size();i++){
                Parameters tempParametersMessage = new Parameters();
                tempParametersMessage.setParameter(paramsMessage.get(i));
                tempParametersMessage.setType(MESSAGE);
                tempParametersMessage = parametersRepository.save(tempParametersMessage);
                listParamsMessage.add(tempParametersMessage);
            }

            Action action = new Action();
            action.setName(name);
            action.setTopic(topic);
            action.setAdminId(adminId);
            action.setLastEditionId(adminId);
            action.setLastEditionDate(new Date());
            action.setMessage(message);
            action.setSort(demoRepository.findById(demoId).get().getAction().size());
            demo.get().setOneAction(action);
            action.setParametersTopics(listParamsTopic);
            action.setParametersMessages(listParamsMessage);
            actionRepository.save(action);
            return FormatedMessage.errorMessage(false);
        }
        return FormatedMessage.errorMessage(true);
    }

    @GetMapping(path="/all")
    public @ResponseBody List<Action> getAllUsers() {
        // This returns a JSON or XML with the users
        return actionRepository.findAllByOrderBySort();
    }

    public List<String> fillNewParameters(String str){
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("\\{\\{[a-zA-Z0-9]*\\}\\}").matcher(str);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }

    @PostMapping(path="/deleteAction")
    public @ResponseBody String deleteAction(@RequestParam Integer actionId){
        if(actionRepository.findById(actionId)!=null){
            actionRepository.deleteById(actionId);
            return FormatedMessage.errorMessage(false);
        }
        return FormatedMessage.errorMessage(true);
    }

    @GetMapping(path="getActionById")
    public @ResponseBody Action getActionById(@RequestParam Integer actionId){
        return actionRepository.findById(actionId).get();
    }

    @Transactional
    @PostMapping(path="/editAction") // Map ONLY GET Requests
    public @ResponseBody
    String editAction (@RequestParam Integer actionId, @RequestParam String name, @RequestParam String topic, @RequestParam String message, @RequestParam Integer lastEditionId) {
        if(actionRepository.findById(actionId).isPresent())
        {
            int i;
            List<String> paramsTopic = fillNewParameters(topic);
            List<Parameters> listParamsTopic = new ArrayList<Parameters>();
            for(i=0;i<paramsTopic.size();i++){
                Parameters tempParametersTopic = new Parameters();
                tempParametersTopic.setParameter(paramsTopic.get(i));
                tempParametersTopic.setType(TOPIC);
                tempParametersTopic = parametersRepository.save(tempParametersTopic);
                listParamsTopic.add(tempParametersTopic);
            }

            List<String> paramsMessage = fillNewParameters(message);
            List<Parameters> listParamsMessage = new ArrayList<Parameters>();
            for(i=0;i<paramsMessage.size();i++){
                Parameters tempParametersMessage = new Parameters();
                tempParametersMessage.setParameter(paramsMessage.get(i));
                tempParametersMessage.setType(MESSAGE);
                tempParametersMessage = parametersRepository.save(tempParametersMessage);
                listParamsMessage.add(tempParametersMessage);
            }
            actionRepository.findById(actionId).get().setName(name);
            actionRepository.findById(actionId).get().setTopic(topic);
            actionRepository.findById(actionId).get().setMessage(message);
            actionRepository.findById(actionId).get().setParametersTopics(listParamsTopic);
            actionRepository.findById(actionId).get().setParametersMessages(listParamsMessage);
            actionRepository.findById(actionId).get().setLastEditionId(lastEditionId);
            actionRepository.findById(actionId).get().setLastEditionDate(new Date());

            return FormatedMessage.errorMessage(false);
        }
        return FormatedMessage.errorMessage(true);
    }

    @Transactional
    @PostMapping(path="/editActionSorting") // Map ONLY GET Requests
    public @ResponseBody
    String editActionSorting(@RequestParam Integer actionId,@RequestParam Integer permutedActionId,@RequestParam Integer actionSort,@RequestParam Integer permutedActionSort) {
        if(actionRepository.findById(actionId).isPresent()&&actionRepository.findById(permutedActionId).isPresent())
        {
            actionRepository.findById(actionId).get().setSort(permutedActionSort);
            actionRepository.findById(permutedActionId).get().setSort(actionSort);
            return FormatedMessage.errorMessage(false);
        }
        return FormatedMessage.errorMessage(true);
    }

    @PostMapping(path="/importAction")
    @ResponseBody
    String importDemo(@RequestParam String actionJson,@RequestParam Integer demoId){
        Optional<Demo> demo = demoRepository.findById(demoId);
        if(demo.isPresent())
        {
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(actionJson).getAsJsonObject();
            String name = jsonObject.get("name").getAsString();
            Integer adminId = jsonObject.get("adminId").getAsInt();
            Integer actionNumber = 1;
            while(actionRepository.findAllByName(name)!=null){
                name = name + String.valueOf(actionNumber);
                actionNumber = actionNumber+1;
            }
            List<Parameters> parametersTopic = new ArrayList<>();
            List<Parameters> parametersMessage = new ArrayList<>();
            JsonArray parametersJsonArrayTopic = jsonObject.get("parametersTopics").getAsJsonArray();
            JsonArray parametersJsonArrayMessage = jsonObject.get("parametersMessages").getAsJsonArray();
            for(int j=0;j<parametersJsonArrayTopic.size();j++){
                Parameters tempParameter = gson.fromJson(parametersJsonArrayTopic.get(j),Parameters.class);
                tempParameter = parametersRepository.save(tempParameter);
                parametersTopic.add(tempParameter);
            }
            for(int k=0;k<parametersJsonArrayMessage.size();k++){
                Parameters tempParameter2 = gson.fromJson(parametersJsonArrayMessage.get(k),Parameters.class);
                tempParameter2 = parametersRepository.save(tempParameter2);
                parametersMessage.add(tempParameter2);
            }
            String topic = jsonObject.get("topic").getAsString();
            String message = jsonObject.get("message").getAsString();
            Integer lastEditionId = jsonObject.get("lastEditionId").getAsInt();
            Action tempAction = new Action();
            tempAction.setName(name);
            tempAction.setTopic(topic);
            tempAction.setMessage(message);
            tempAction.setSort(demoRepository.findById(demoId).get().getAction().size());
            tempAction.setAdminId(adminId);
            tempAction.setLastEditionId(lastEditionId);
            tempAction.setLastEditionDate(new Date());
            tempAction.setParametersTopics(parametersTopic);
            tempAction.setParametersMessages(parametersMessage);
            demo.get().setOneAction(tempAction);
            actionRepository.save(tempAction);
            return FormatedMessage.errorMessage(false);
        }
        return FormatedMessage.errorMessage(true);
    }

    @GetMapping(path="/findByName")
    public @ResponseBody Iterable<Action> findAllByName(@RequestParam String name) {
        // This returns a JSON or XML with the users
        return actionRepository.findAllByName(name);
    }
}

