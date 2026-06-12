// Copyright 1986-2018 Xilinx, Inc. All Rights Reserved.
// --------------------------------------------------------------------------------
// Tool Version: Vivado v.2018.3 (win64) Build 2405991 Thu Dec  6 23:38:27 MST 2018
// Date        : Mon Oct 13 22:42:29 2025
// Host        : anbera running 64-bit major release  (build 9200)
// Command     : write_verilog -force -mode synth_stub {c:/Users/yanha/Desktop/Vivado
//               project/project_1_3_mux4x1.block_design/project_1_3_mux4x1.block_design.srcs/sources_1/bd/mux4x1_block/ip/mux4x1_block_notgate_2_0_0/mux4x1_block_notgate_2_0_0_stub.v}
// Design      : mux4x1_block_notgate_2_0_0
// Purpose     : Stub declaration of top-level module interface
// Device      : xc7a35tcpg236-1
// --------------------------------------------------------------------------------

// This empty module with port declaration file causes synthesis tools to infer a black box for IP.
// The synthesis directives are for Synopsys Synplify support to prevent IO buffer insertion.
// Please paste the declaration into a Verilog source file or add the file as an additional source.
(* X_CORE_INFO = "notgate_2,Vivado 2018.3" *)
module mux4x1_block_notgate_2_0_0(in1, in2, out1, out2)
/* synthesis syn_black_box black_box_pad_pin="in1,in2,out1,out2" */;
  input in1;
  input in2;
  output out1;
  output out2;
endmodule
