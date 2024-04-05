module Mem #(parameter MEM_SIZE = 262144, _SEED = 225526)(input wire CLK, input wire[17:0] A2, inout wire[15:0] D2, inout wire[1:0] C2, input wire Reset);
  integer SEED = _SEED;
  reg[7:0] Mem[MEM_SIZE - 1:0];
  reg dataFlag = 0, ctrlFlag = 0;
  integer memData;
  integer memComand;
  assign D2 = dataFlag == 1 ? memData : D2;
  assign C2 = ctrlFlag == 1 ? memComand : C2;

  initial begin
    _Reset();
  end

  always @(Reset) begin
    _Reset();
  end

  task _Reset();    
  for (integer i = 0; i < MEM_SIZE; i++) begin
    Mem[i] = $random(SEED)>>16;
    $display("[%d] %d", i, Mem[i]);  
  end
  endtask
  
  always @(C2) begin
    memComand = C2;
    if (memComand == 2) begin
        #99
        dataFlag = 1;
        memData = Mem[A2];
        #1
        dataFlag = 0;
    end 
    else begin
        Mem[A2] = memData;
        #99
        ctrlFlag = 1;
        memComand = 1;
        #1
        ctrlFlag = 0;
    end
  end 
endmodule