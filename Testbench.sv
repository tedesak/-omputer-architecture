`include "Mem.v"

module testbench();
 wire CLK;
 reg[17:0] A1 = 0, A2 = 0;
 reg[15:0] D1 = 0, D2;
 reg[2:0] C1 = 0;
 wire[1:0] C2;
 reg Reset = 0;

 reg[1:0] CtrlReg = 2;
// CPU _CPU(CLK, D1, C1, A1);
// Cache _Cache(CLK, A1, D1, C1, D2, C2, A2, Reset);
 Mem _MEM(CLK, A2, D2, C2, Reset);
 initial begin
    $display("%d %d %d", A2, D2, C2);
    $finish;
 end


// initial begin
//   Reset = 1;
// end



  //  always @() begin
    
  //  end


endmodule