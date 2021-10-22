/*
Copyright (C) 2019-2021 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController.Bus

import chisel3._
import chisel3.util._

class WishboneMaster(val addrWidth : Int, val dataWidth : Int) extends Bundle{
  /* data */
  val dat_i = Input(UInt(dataWidth.W))
  val dat_o = Output(UInt(dataWidth.W))
  /* control */
  val cyc_o = Output(Bool())
  val stb_o = Output(Bool())
  val we_o = Output(Bool())
  val adr_o = Output(UInt((addrWidth - log2Ceil(dataWidth / 8)).W))
  val sel_o = Output(UInt((dataWidth / 8).W))
  val ack_i = Input(Bool())
  val stall_i = Input(Bool())
  val err_i = Input(Bool())
}
