# ToyMachine
## Screen Shot                                                                                                                   
![alt text](https://github.com/itlearningresources/ToyMachine/blob/main/Toy.png) 
My Mod of TOY.

TOY is an imaginary machine (created at Princeton) that is very similar to ancient computers.
Started with the source code from the princeton site and expanded it extensively.

"For teachers:
This online content. Everything on these pages is freely available. We ask only that you adhere to normal academic traditions
of attribution if you adapt this content in your own course. One best practice is to just provide links to our pages."

Attributions: https://introcs.cs.princeton.edu/java/home/





    Command Line Commands
    
    H       Help                    	/       Find                    
    IPL     Initial Program Load    	LOG     Show Log Buffer         
    CRASH   Crash Application       	R       Show Program Trace      
    S       Single Step             	P       Show Program as read in 
    G       Run Program             	DECODE  Decode Memory           
    BREAK   Set/Clear Breakpoint    	NEXT     Show Next (Decoded) instruction to be executed
    TEST     Test Command           	RELOAD  Reload Current File     
    COLOR   Initial Program Load    	LOAD     Load Program From File 
    CLEAR   Clear Trace Window      	ROWS    Set Display Rows        
    Q       Quit                    	MRO     Show R0 Indirect Memory 
    T       Move to Top             	M       Show Memory             
    B       Move to Bottom          	STATUS  Show Status             
    U       Move Up                 	E       Edit Memory <Address>   
    D       Move Down               	
    F       Find                    	
    
    Sample Program
    
    PRAGMA MEMORY 0000 BOOT
    0700 0000
    PRAGMA PAGESIZE 0010 XXX
    PRAGMA MEMORY 0500 DATA
    PRAGMA STRING Michael
    PRAGMA MEMORY 0700 PROGRAM
    1010 0055
    0410 0000 TOL
    3500 ADDMETHOD
    7021 0000
    2110 ELOOP
    2000 TOL
    PRAGMA HERE ELOOP
    NOP  0000
    NOP  0000
    NOP  0000
    1020 0040
    0420 0000 TOLA
    2120 ELOOPA
    2000 TOLA
    1020 0099 ELOOPA
    NOP  0000
    NOP  0000
    NOP  0000
    0000 0000 
    0310 0000 
    PRAGMA SUBROUTINE 0800 ADDMETHOD
    NOP  0000
    
    
    Pragmas
    
    PRAGMA STRING <string>
    PRAGMA MEMORY <hexaddr> <label>
    PRAGMA SUBROUTINE <hexaddr> <label>
    PRAGMA HERE <label>
    
    Mnemonics
    
    HALT       Halt
    HLT        Halt
    ADD        Addi
    SUB        Subtract
    INC        Increment
    DEC        Decrement
    ACM        Acumulate
    DCM        Decumulate
    LRA        Load register with word
    LRM        Load register with memory
    SRM        Store register to memory
    IRM        Indirect register to memory
    ILD        Indirect Load
    LIR        Load index register
    JMP        Jump to address in word
    BIZ        Branch if Zero
    BINZ       Branch if not Zero
    PLZ        Pop and Link if Zero
    PLNZ       Pop and Link if not Zero
    BIF        Branch if Positive
    BRI        Branch indirect
    JLK        Branch indirect
    PSHW       Push word
    PSHR       Push register
    POPR       Pop register
    NOP        No Operation
    SYS        System Call
    
    Instructions
    
    
    Halt
    0x00 halt                              haltflag = true                         
    
    Math and Accumulator
    0x01 add                               reg[d] = reg[s] + reg[t]                
    0x02 subtract                          reg[d] = reg[s] - reg[t]                
    0x03 increment register                reg[d]++                                
    0x04 decrement register                reg[d]--                                
    0x05 accumulate                        reg[d] = reg[d] + reg[s]                
    0x06 deccumulate                       reg[d] = reg[d] + reg[s]                
    
    Load and Store
    0x10 load register with addr           reg[d] = word                           
    0x11 load register with memory         reg[d] = mem[word]                      
    0x14 store reg to mem                  mem[addr] = reg[d]                      
    0x15 store reg to mem indirect         mem[reg[d] & 0x0FFFF] = reg[s]          
    0x16 load indirect                     reg[d] = mem[reg[s] & 0xFFFF]           
    
    Branch and Jump
    0x20 jump                              pc = addr                               
    0x21 branch if zero                    if (reg[d] == 0) pc = addr              
    0x22 branch if not zero                if (reg[d] != 0) pc = addr              
    0x23 pop and link if zero              if (reg[d] == 0) pc = popstk            
    0x24 pop and link if not zero          if (reg[d] != 0) pc = popstk            
    0x25 branch if pos                     if (reg[d] >  0) pc = addr              
    0x26 jump indirect                     pc = reg[d]                             
    0x27 jump and link                     reg[d] = pc; pc = addr                  
    
    Stack
    0x30 push address                      push addr                               
    0x31 push register                     push reg[d]                             
    0x32 pop to register                   pop to reg[d]                           
    0x34 Push This                         push this addr                          
    0x35 push pc and link                  push pc and pc = addr                   
    0x36 pop and link                      return                                  
    
    Bitwise and Shift
    0x40 bitwise and                       reg[d] = reg[s] & reg[t]                
    0x41 bitwise or                        reg[d] = reg[s] ^ reg[t]                
    0x42 shift left                        reg[d] = reg[s] << reg[t]               
    0x43 shift right                       reg[d] = reg[s] >> reg[t]               
    0x44 shift reg left                    reg[d] = reg[d] << 1                    
    0x45 shift reg right                   reg[d] = reg[d] >> 1                    
    
    NOP
    0x50 NOP                               NOP                                     
    
    NOP
    0x65 int to ascii                      int to ascii                            
    0x66 mem int to ascii                  mem int to ascii                        
    
    Systems Calls
    0x70 system call                       system call                             
    

