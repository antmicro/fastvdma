/*
Copyright (C) 2019-2022 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController.CSR

import chisel3._
import DMAController.DMAConfig.DMATop
import DMAUtils.DMAModule

class SimpleCSR extends DMAModule{
  val io = IO(new Bundle{
    val csr = Flipped(new CSRRegBundle())
    val value = Output(UInt(DMATop.controlDataWidth.W))
  })

  val reg = RegInit(0.U(DMATop.controlDataWidth.W))

  io.csr.dataIn := reg
  io.value := reg

  when(io.csr.dataWrite){
    reg := io.csr.dataOut
  }

}

object SimpleCSR {
  def apply(csrCtl : CSRRegBundle): UInt = {
    val csr = Module(new SimpleCSR())

    csr.io.csr <> csrCtl

    csr.io.value
  }
}
