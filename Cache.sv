`include "Mem.v"

module Cache#(parameter _SEED = 225526, Cache_set_count = 64, Cache_line_size = 128, cache_tag_size = 8)(input wire CLK, input wire[17:0] A1, inout wire[15:0] D1, inout wire[2:0] C1, input wire Reset);
  integer SEED = _SEED;
  reg[138 - 1:0] cache[Cache_set_count - 1:0][1:0];
  reg dataFlag1 = 0, ctrlFlag1 = 0,
    dataFlag2 = 0, ctrlFlag2 = 0;
  integer memData1, memData2;
  integer memComand1, memComand2;
  wire[15:0] D2;
  wire[1:0] C2;
  reg[17:0] A2;
  assign D1 = dataFlag1 == 1 ? memData1 : D1;
  assign C1 = ctrlFlag1 == 1 ? memComand1 : C1;
  assign D2 = dataFlag2 == 1 ? memData2 : D2;
  assign C2 = ctrlFlag2 == 1 ? memComand2 : C2;
  Mem _MEM(CLK, A2, D2, C2, Reset);
  
  initial begin
    _Reset();
  end

  always @(Reset) begin
    _Reset();
  end

  task _Reset();    
  for (integer i = 0; i < Cache_set_count; i++) begin
    cache[i][0] = $random(SEED)>>16;
    cache[i][1] = $random(SEED)>>16;
    $display("[%d] %d", i, cache[i][0], cache[i][1]);  
  end
  endtask
  
  always @(C1) begin
    memComand1 = C1;
    if (memComand1 == 1) begin
        #1
        ctrlFlag2 = 1;
        memComand2 = 2;
        A2 = A1;
        #1
        ctrlFlag2 = 0;
        dataFlag1 = 1;
        ctrlFlag1 = 1;
        #1
        memData1 = memData2;
        memComand2 = 1;
        #1
        dataFlag1 = 0;
        ctrlFlag1 = 0;
    end 
    else if (memComand1 == 2) begin
        #1
        ctrlFlag2 = 1;
        memComand2 = 2;
        A2 = A1;
        #1
        ctrlFlag2 = 0;
        dataFlag1 = 1;
        ctrlFlag1 = 1;
        #1
        memData1 = memData2;
        memComand2 = 1;
        #1
        dataFlag1 = 0;
        ctrlFlag1 = 0;
    end 
    else if (memComand1 == 3) begin
        #1
        ctrlFlag2 = 1;
        memComand2 = 2;
        A2 = A1;
        #1
        ctrlFlag2 = 0;
        dataFlag1 = 1;
        ctrlFlag1 = 1;
        #1
        memData1 = memData2;
        memComand2 = 1;
        #1
        dataFlag1 = 0;
        ctrlFlag1 = 0;
    end 
    else if (memComand1 == 4) begin
        
    end 
    else if (memComand1 == 5) begin
        #1
        ctrlFlag2 = 1;
        dataFlag2 = 1;
        memComand2 = 3;
        memData2 = D1;
        A2 = A1;
        #1
        ctrlFlag2 = 0;
        dataFlag2 = 0;
        dataFlag1 = 1;
        ctrlFlag1 = 1;
        #1
        memData1 = memData2;
        memComand1 = memComand2;
        #1
        dataFlag1 = 0;
        ctrlFlag1 = 0;
    end 
    else if (memComand1 == 6) begin
        #1
        ctrlFlag2 = 1;
        dataFlag2 = 1;
        memComand2 = 3;
        memData2 = D1;
        A2 = A1;
        #1
        ctrlFlag2 = 0;
        dataFlag2 = 0;
        dataFlag1 = 1;
        ctrlFlag1 = 1;
        #1
        memData1 = memData2;
        memComand1 = memComand2;
        #1
        dataFlag1 = 0;
        ctrlFlag1 = 0;
    end 
    else if (memComand1 == 7) begin
        #1
        ctrlFlag2 = 1;
        dataFlag2 = 1;
        memComand2 = 3;
        memData2 = D1;
        A2 = A1;
        #1
        ctrlFlag2 = 0;
        dataFlag2 = 0;
        dataFlag1 = 1;
        ctrlFlag1 = 1;
        #1
        memData1 = memData2;
        memComand1 = memComand2;
        #1
        dataFlag1 = 0;
        ctrlFlag1 = 0;
    end
  end 
endmodule