package unit.test.naming;

public class HelloWorldImpl extends HelloWorldPOA
{
    private int id;

    public String getHelloMessage()
    {
        return "Greetings from the land of Zen!";
    }

    public int id()
    {
        return id;
    }

    public void id(int id)
    {
        this.id = id;
    }
}

