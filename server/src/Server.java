
import java.io.*;
import java.util.*;
import java.net.*;

// Server class
public class Server
{

    static ArrayList<ClientHandler> clients = new ArrayList<>();

    static boolean end = false;

    static Puzzle puz = new Puzzle();
    static int[][] temp = puz.generate();
    static int[][] resolve = Puzzle.getResolve();

    static String showInfo(){
        String response = "";
        for (int i = 0; i <clients.size() ; i++)
            response += ("-player"+ (i+1) + " Score: "+clients.get(i).score+"  ");
        response+="\n ------------------------------------------ \n";
        return response;
    }

    static String showSoudoko(){
        String response = "";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                response += temp[i][j];
                response+= " ";
            }
            response+="\n";
        }
        return response;
    }

    static String showResolve(){
        String response = "";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                response += resolve[i][j];
                response+= " ";
            }
            response+="\n";
        }
        return response;
    }

    static void checkEnd(){
        boolean flag=false;
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                if (temp[i][j]!=resolve[i][j])
                    flag=true;


        if (!flag)
            end=true;
    }


    public static void main(String[] args) throws IOException
    {
        System.out.println(showResolve());

        boolean first = true;

        // server is listening on port 5056
        ServerSocket ss = new ServerSocket(5056);

        // running infinite loop for getting
        // client request
        while (true)
        {
            Socket s = null;

            try
            {
                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                // create a new thread object
                ClientHandler t = new ClientHandler(s, dis, dos);

                clients.add(t);

                // Invoking the start() method
                t.start();

                if (first){
                    t.canResponse=true;
                    first = false;
                    }


            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}

// ClientHandler class
class ClientHandler extends Thread
{

    static final String line = "\n------------------------------------------\n";

    boolean canResponse = false;

    int score=0;

    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;


    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run()
    {
        String received;
        while (true)
        {
            try {

                if(!canResponse) {
                    dos.writeUTF("not your order, please wait");



                    while (!canResponse){
                       System.out.println(" while "+Server.clients.indexOf(this)+" :" + canResponse);
                        Thread.sleep(3000);
                    }
                        if (Server.end){
                            dos.writeUTF("The game was end..."+line+Server.showInfo()+"The resolve is:\n"+ Server.showResolve());
                            close();
                        } else {
                            continue;
                        }




                }
                else {
                    // Ask user what he wants
                    dos.writeUTF(Server.showInfo()+Server.showSoudoko()+line+"First enter the coordinates of the number and then the value of the number with a space. for example:23 9\n");
                }
                // receive the answer from client
                received = dis.readUTF();

                if(received.equals("Exit"))
                {
                    close();
                    break;
                }


                // write on output stream based on the
                // answer from the client
                if (received.length()!=4)
                    dos.writeUTF("Invalid input, Please try again");
                else if(Server.temp[received.charAt(0)-49][received.charAt(1)-49]!=0)
                    dos.writeUTF("This block is full, Please try again");
                else if(Server.resolve[received.charAt(0)-49][received.charAt(1)-49]!=received.charAt(3)-48){
                    dos.writeUTF("Incorrect resolve");
                    setCanResponse();
                }else {
                    dos.writeUTF("Correct resolve, Your score increase to: "+ ++this.score+"\n");
                    Server.temp[received.charAt(0)-49][received.charAt(1)-49]=received.charAt(3)-48;
                    setCanResponse();
                }

                Server.checkEnd();
                if (Server.end){
                    dis.readUTF();
                    dos.writeUTF("The game was end..."+line+Server.showInfo()+"The resolve is:\n"+ Server.showResolve());
                    for (int i = 0; i <Server.clients.size() ; i++) {
                        Server.clients.get(i).canResponse=true;
                    }
                    close();
                }







            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    void setCanResponse(){
        this.canResponse=false;

        int index = Server.clients.indexOf(this);

        System.out.println("this order: "+ (index+1) );

        if (index==Server.clients.size()-1)
            Server.clients.get(0).canResponse=true;
        else
            Server.clients.get(index+1).canResponse=true;

        for (int i = 0; i <Server.clients.size() ; i++) {
            System.out.println((i)+" : "+Server.clients.get(i).canResponse);
        }
    }

    void close() throws IOException {
        System.out.println("Client " + this.s + " sends exit...");
        System.out.println("Closing this connection.");
        this.s.close();
        System.out.println("Connection closed");
    }


}

 class Puzzle {

     static int a[][] = new int[9][9];
     static int b[][] = new int[9][9];

     public int[][] generate() {

         int p = 1;
         Random r = new Random();
         int i1 = r.nextInt(8);
         int firstval = i1;

             int x = firstval, v = 1;

             for (int i = 0; i < 9; i++) {
                 for (int j = 0; j < 9; j++) {
                     if ((x + j + v) <= 9)
                         a[i][j] = j + x + v;
                     else
                         a[i][j] = j + x + v - 9;
                     if (a[i][j] == 10)
                         a[i][j] = 1;

                 }
                 x += 3;
                 if (x >= 9)
                     x = x - 9;

                 if (i == 2) {
                     v = 2;
                     x = firstval;
                 }
                 if (i == 5) {
                     v = 3;
                     x = firstval;
                 }

             }

//             b[0][0] = a[0][0];
//             b[8][8] = a[8][8];
//             b[0][3] = a[0][3];
//             b[0][4] = a[0][4];
//             b[1][2] = a[1][2];
//             b[1][3] = a[1][3];
//             b[1][6] = a[1][6];
//             b[1][7] = a[1][7];
//             b[2][0] = a[2][0];
//             b[2][4] = a[2][4];
//             b[2][8] = a[2][8];
//             b[3][2] = a[3][2];
//             b[3][8] = a[3][8];
//             b[4][2] = a[4][2];
//             b[4][3] = a[4][3];
//             b[4][5] = a[4][5];
//             b[4][6] = a[4][6];
//             b[5][0] = a[5][0];
//             b[5][6] = a[5][6];
//             b[6][0] = a[6][0];
//             b[6][4] = a[6][4];
//             b[6][8] = a[6][8];
//             b[7][1] = a[7][1];
//             b[7][2] = a[7][2];
//             b[7][5] = a[7][5];
//             b[7][6] = a[7][6];
//             b[8][4] = a[8][4];
//             b[8][5] = a[8][5];
//             b[0][0] = a[0][0];
//             b[8][8] = a[8][8];

                     b[0][3] = a[0][3];
                     b[0][4] = a[0][4];
                     b[1][2] = a[1][2];
                     b[1][3] = a[1][3];
                     b[1][6] = a[1][6];
                     b[1][7] = a[1][7];
                     b[1][8] = a[1][8];
                     b[2][0] = a[2][0];
                     b[2][4] = a[2][4];
                     b[2][8] = a[2][8];
                     b[3][2] = a[3][2];
                     b[3][5] = a[3][5];
                     b[3][8] = a[3][8];
                     b[4][0] = a[4][0];
                     b[4][2] = a[4][2];
                     b[4][3] = a[4][3];
                     b[4][4] = a[4][4];
                     b[4][5] = a[4][5];
                     b[4][6] = a[4][6];
                     b[5][0] = a[5][0];
                     b[5][1] = a[5][1];
                     b[5][4] = a[5][4];
                     b[5][6] = a[5][6];
                     b[6][0] = a[6][0];
                     b[6][4] = a[6][4];
                     b[6][6] = a[6][6];
                     b[6][8] = a[6][8];
                     b[7][0] = a[7][0];
                     b[7][1] = a[7][1];
                     b[7][2] = a[7][2];
                     b[7][5] = a[7][5];
                     b[7][6] = a[7][6];
                     b[8][2] = a[8][2];
                     b[8][4] = a[8][4];
                     b[8][5] = a[8][5];

         return b;
     }

     static int[][]  getResolve(){
         return a;
     }


 }