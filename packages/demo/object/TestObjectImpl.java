package demo.object;

class TestObjectImpl extends TestObjectPOA {

    String name;
    TestObjectImpl(String _name){
        name = _name;
    }
    public String getName(){
        return name;
    }
}

