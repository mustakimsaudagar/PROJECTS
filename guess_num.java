import java.util.Random;
import java.util.Scanner;
public class guess_num{
    public static void main(String[] args) {
        System.out.println("Guess number between 0 to 100");
        Scanner sc= new Scanner(System.in);
        Random rand = new Random();
        int randomInt = rand.nextInt(100);
        int x=0;
        while (true) {
            int a = sc.nextInt();
            x++;

            if(randomInt==a){
                System.out.println("Congratulations Your are win in "+x+" attempts");
                break;
            }
            else if(randomInt>a){
                System.out.println("target is higher than your guess \ntry again");
            }
            else{
                System.out.println("target is Lower than your guess \ntry again");
                
            }
        

        }
        sc.close();
        
        
    }
}