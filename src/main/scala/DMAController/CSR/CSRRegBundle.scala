/*
Copyright (C) 2019-2023 Antmicro

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

class CSRRegBundle() extends Bundle{
  val dataRead = Output(Bool())
  val dataOut = Output(UInt(DMATop.controlDataWidth.W))
  val dataWrite = Output(Bool())
  val dataIn = Input(UInt(DMATop.controlDataWidth.W))
}
