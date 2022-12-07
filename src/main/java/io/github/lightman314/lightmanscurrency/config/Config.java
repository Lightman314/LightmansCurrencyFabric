package io.github.lightman314.lightmanscurrency.config;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.config.pathing.*;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.util.JsonHelper;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Config {

    private static final List<Config> registeredConfigs = new ArrayList<>();

    public static final String CONFIG_FOLDER = "config/";
    private final boolean overrideFolder;
    private final String fileName;

    private boolean loaded = false;
    public boolean isLoaded() { return this.loaded; }

    private final ConfigBuilder builder;

    protected Config(String fileName, ConfigBuilder builder) { this(fileName, false, builder); }
    protected Config(String fileName, boolean overrideFolder, ConfigBuilder builder) {
        this.fileName = fileName;
        this.overrideFolder = overrideFolder;
        this.builder = builder;
    }

    protected final void lock() { this.builder.lock(); }

    public final List<IConfigAction> getActions() { return this.builder.actions; }

    public final String getFilePath() {
        String filePath = this.overrideFolder ? this.fileName : CONFIG_FOLDER + this.fileName;
        if(!filePath.endsWith(".json"))
            filePath += ".json";
        return filePath;
    }

    private JsonObject getFileJson()
    {
        try{
            File file = new File(this.getFilePath());
            if(file.exists())
                return JsonHelper.deserialize(Files.readString(file.toPath()));
        } catch(Throwable t) { LightmansCurrency.LogError("Error loading config json at '" + this.getFilePath() + "'!"); }
        return new JsonObject();
    }

    private void writeFileJson() {
        try{
            JsonStack jsonStack = new JsonStack();
            for(IConfigAction option : this.getActions())
                option.writeAction(jsonStack);
            File file = new File(this.getFilePath());
            FileUtil.writeStringToFile(file, FileUtil.GSON.toJson(jsonStack.getRoot()));
        } catch(Throwable t) { LightmansCurrency.LogError("Error write config file at '" + this.getFilePath() + "'!");  }
    }

    public void reloadValues() {
        try{
            JsonStack jsonStack = new JsonStack(this.getFileJson());
            for(IConfigAction option : this.getActions())
                option.readAction(jsonStack);
        } catch(Throwable t) { LightmansCurrency.LogError("Error loading config file at '" + this.getFilePath() + "'!"); }
        this.writeFileJson();
        this.loaded = true;
    }

    public static void register(Config config) {
        if(registeredConfigs.contains(config) || config == null)
            return;
        registeredConfigs.add(config);
    }

    public static void reloadConfigs() {
        for(Config c : registeredConfigs)
        {
            if(c != null)
                c.reloadValues();
        }
    }

    protected static class ConfigBuilder {

        private Config config;
        public void init(Config config){ this.config = config; }

        private ConfigBuilder() { }
        public static ConfigBuilder create() { return new ConfigBuilder(); }

        private int commentCount = 0;
        private final List<String> pendingComments = new ArrayList<>();
        private final List<IConfigAction> actions = new ArrayList<>();

        private boolean locked = false;
        protected void lock() { this.locked = true; }

        private void addAction(IConfigAction action) {
            if(this.locked)
            {
                LightmansCurrency.LogWarning("Tried to add an action to the config after it's been locked!");
                return;
            }
            if(action == null)
            {
                LightmansCurrency.LogWarning("Tried to add a null action to the config!");
                return;
            }
            this.actions.add(action);
        }

        private void tryAddComments() {
            if(this.pendingComments.size() > 0)
            {
                for (String pendingComment : this.pendingComments)
                    this.addAction(CommentAction.of("_c" + ++commentCount, pendingComment));
                this.pendingComments.clear();
            }
        }

        public ConfigBuilder comment(String... comments) { this.pendingComments.addAll(Arrays.asList(comments)); return this; }

        public ConfigBuilder push(String path) { this.tryAddComments(); this.addAction(PushAction.of(path)); return this; }

        public ConfigBuilder pop() { this.addAction(PopAction.of()); return this; }

        public <T extends ConfigOption<?>> T option(String name, T option) {
            if(option == null)
            {
                LightmansCurrency.LogError("Attempted to add a null config option at '" + name + "'!");
                return null;
            }
            option.appendPendingComments(this.pendingComments);
            this.tryAddComments();
            this.addAction(option);
            option.init(name, this.config);
            return option;
        }

    }

}
