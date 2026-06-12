// Copyright 1986-2018 Xilinx, Inc. All Rights Reserved.
// --------------------------------------------------------------------------------
// Tool Version: Vivado v.2018.3 (win64) Build 2405991 Thu Dec  6 23:38:27 MST 2018
// Date        : Mon Oct 13 22:42:29 2025
// Host        : anbera running 64-bit major release  (build 9200)
// Command     : write_verilog -force -mode synth_stub -rename_top mux4x1_block_andgate_9_0 -prefix
//               mux4x1_block_andgate_9_0_ mux4x1_block_andgate_5_0_stub.v
// Design      : mux4x1_block_andgate_5_0
// Purpose     : Stub declaration of top-level module interface
// Device      : xc7a35tcpg236-1
// --------------------------------------------------------------------------------

// This empty module with port declaration file causes synthesis tools to infer a black box for IP.
// The synthesis directives are for Synopsys Synplify support to prevent IO buffer insertion.
// Please paste the declaration into a Verilog source file or add the file as an additional source.
(* X_CORE_INFO = "andgate,Vivado 2018.3" *)
module mux4x1_block_andgate_9_0(a, b, c)
/* synthesis syn_black_box black_box_pad_pin="a[0:0],b[0:0],c[0:0]" */;
  input [0:0]a;
  input [0:0]b;
  output [0:0]c;
endmodule
