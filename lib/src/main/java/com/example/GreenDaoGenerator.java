package com.example;


import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoGenerator {
    public static String path = "/Users/opticalix/Work/Projects/WidgetReminder/src";
    public static String packageName = "com.opticalix";

    public static void main(String[] args) throws Exception {
//        System.out.print("done");
        Schema schema = new Schema(1, packageName + ".storage.bean");
        schema.setDefaultJavaPackageDao(packageName + ".storage.dao");
        schema.enableKeepSectionsByDefault();//实体类在每一次生成器运行的时候都会被覆盖。可以调用此方法让自己添加的代码不被覆盖
        addNote(schema);
        new DaoGenerator().generateAll(schema, path);
    }

    private static void addNote(Schema schema) {
        Entity entity = schema.addEntity("Note");
        entity.addIdProperty().autoincrement();
        entity.addStringProperty("content");
        entity.addDateProperty("create_date");
        entity.addDateProperty("update_date");
        entity.addBooleanProperty("lock");

//        Entity tip = schema.addEntity("TipDetail");
//        tip.addIdProperty().autoincrement();
//        tip.addDateProperty("create_date");
//        tip.addDateProperty("update_date");
//        tip.addStringProperty("tip");
//        tip.addStringProperty("pic");
//        tip.addStringProperty("voice");
//
//        Property guide_id = tip.addLongProperty("guide_id").notNull().getProperty();
//        tip.addToOne(entity, guide_id);
//        entity.addToMany(tip, guide_id).setName("tips");
    }
}
