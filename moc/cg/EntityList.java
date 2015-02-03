package moc.cg;

import java.util.ArrayList;
import java.util.List;

public class EntityList {
    private ArrayList<EntityCode> entities = new ArrayList<>();

    public void prepend(EntityCode entity) {
        entities.add(0, entity);
    }

    public List<EntityCode> getList() {
        return entities;
    }
}
