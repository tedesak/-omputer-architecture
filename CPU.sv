module CPU();
  wire[2:0] outComand, inComand;
  wire[4:0] outAddr, inAddr;
  wire[15:0] outData, inData;
  
  Cache cache(.outComand1(outComand), .outAddr1(outAddr), .outData1(outData), .inComand1(inComand), .inAddr1(inAddr), .inData1(inData));
  
  initial begin
    
  end
  
  always @(inComand != 0) begin
    tag = inAddr[17:10];
    set = inAddr[9:4];
    offset = inAddr[3:0];
    case(inComand)
      1: begin
        
      end
      2: begin
        
      end
      3: begin
        
      end
      endcase
  end
  
endmodule