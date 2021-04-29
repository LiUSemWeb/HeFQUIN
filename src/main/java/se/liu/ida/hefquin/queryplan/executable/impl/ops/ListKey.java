package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.ArrayList;

class ListKey<T> {
    private ArrayList<T> list;

    public ListKey(ArrayList<T> list) {
        this.list = (ArrayList<T>) list.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        for (int i = 0; i < this.list.size(); i++) {
            T item = this.list.get(i);
            result = prime * result + ((item == null) ? 0 : item.hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListKey<?> listKey = (ListKey<?>) o;
        return list.equals(listKey.list);
    }
}