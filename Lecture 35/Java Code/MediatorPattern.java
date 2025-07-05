import java.util.*;

// ─────────────── Mediator Interface ───────────────
interface IMediator {
    void registerColleague(Colleague c);
    void send(String from, String msg);
    void sendPrivate(String from, String to, String msg);
}

// ─────────────── Colleague Interface ───────────────
abstract class Colleague {
    protected IMediator mediator;  
    
    public Colleague(IMediator m) {
        mediator = m;
        mediator.registerColleague(this);
    }
    
    public abstract String getName();
    public abstract void send(String msg);
    public abstract void sendPrivate(String to, String msg);
    public abstract void receive(String from, String msg);
}

// Simple Pair class
class Pair<T, U> {
    public final T first;
    public final U second;
    
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
}

// ─────────────── Concrete Mediator ───────────────
class ChatMediator implements IMediator {
    private List<Colleague> colleagues;
    private List<Pair<String, String>> mutes; // (muter, muted)
    
    public ChatMediator() {
        colleagues = new ArrayList<>();
        mutes = new ArrayList<>();
    }
    
    public void registerColleague(Colleague c) {
        colleagues.add(c);
    }
    
    public void mute(String who, String whom) {
        mutes.add(new Pair<>(who, whom));
    }
    
    public void send(String from, String msg) {
        System.out.println("[" + from + " broadcasts]: " + msg);
        for (Colleague c : colleagues) {
            // Don't send msg to itself.
            if (c.getName().equals(from)) {
                continue;
            }

            boolean isMuted = false;
            // Ignore if person is muted
            for (Pair<String, String> p : mutes) {
                if (from.equals(p.second) && c.getName().equals(p.first)) {
                    isMuted = true;
                    break;
                }
            }
            if (!isMuted) {
                c.receive(from, msg);
            }
        }
    }
    
    public void sendPrivate(String from, String to, String msg) {
        System.out.println("[" + from + "→" + to + "]: " + msg);
        for (Colleague c : colleagues) {
            if (c.getName().equals(to)) {
                for (Pair<String, String> p : mutes) {
                    //Dont send if muted
                    if (from.equals(p.second) && to.equals(p.first)) {
                        System.out.println("\n[Message is muted]\n");
                        return;
                    }
                }
                c.receive(from, msg);
                return;
            }
        }
        System.out.println("[Mediator] User \"" + to + "\" not found]");
    }
}

// ─────────────── Concrete Colleague ───────────────
class User extends Colleague {
    private String name;
    
    public User(String n, IMediator m) {
        super(m);
        name = n;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void send(String msg) {
        mediator.send(name, msg);
    }
    
    @Override
    public void sendPrivate(String to, String msg) {
        mediator.sendPrivate(name, to, msg);
    }
    
    @Override
    public void receive(String from, String msg) {
        System.out.println("    " + name + " got from " + from + ": " + msg);
    }
}

// ─────────────── Demo ───────────────
public class MediatorPattern {
    public static void main(String[] args) {
        ChatMediator chatRoom = new ChatMediator();
        
        User user1 = new User("Rohan", chatRoom);
        User user2 = new User("Neha", chatRoom);
        User user3 = new User("Mohan", chatRoom);
        
        // Rohan mutes Mohan
        chatRoom.mute("Rohan", "Mohan");
        
        // broadcast from Rohan
        user1.send("Hello Everyone!");
        
        // private from Mohan to Neha
        user3.sendPrivate("Neha", "Hey Neha!");
    }
}