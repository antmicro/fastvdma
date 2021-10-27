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

package DMAController.CSR

import DMAController.DMAConfig._
import chisel3._

class CSR(val addrWidth : Int) extends Module{
  val io = IO(new Bundle{
    val csr: Vec[CSRRegBundle] = Vec(DMATop.controlRegCount, new CSRRegBundle())
    val bus = Flipped(new CSRBusBundle)
  })

  val data = WireInit(0.U(DMATop.controlDataWidth.W))

  io.bus.dataIn := data

  for(i <- 0 until DMATop.controlRegCount){
    when(io.bus.addr === i.U && io.bus.read){
      data := io.csr(i).dataIn
      io.csr(i).dataRead := true.B
    }.otherwise{
      io.csr(i).dataRead := false.B
    }

    when(io.bus.addr === i.U && io.bus.write){
      io.csr(i).dataOut := io.bus.dataOut
      io.csr(i).dataWrite := true.B
    }.otherwise{
      io.csr(i).dataWrite := false.B
      io.csr(i).dataOut := 0.U
    }
  }
}
