/*
Copyright (C) 2019-2025 Antmicro

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
import chisel3.util.log2Ceil

class CSRBusBundle(controlRegCount: Int, controlDataWidth: Int) extends Bundle {
  val addr = Output(UInt(log2Ceil(controlRegCount).W))
  val dataOut = Output(UInt(controlDataWidth.W))
  val dataIn = Input(UInt(controlDataWidth.W))
  val write = Output(Bool())
  val read = Output(Bool())
}
