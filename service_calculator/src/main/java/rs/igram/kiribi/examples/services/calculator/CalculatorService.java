/* 
 * MIT License
 * 
 * Copyright (c) 2020 Igram, d.o.o.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
package rs.igram.kiribi.examples.services.calculator;

import rs.igram.kiribi.service.Descriptor;
import rs.igram.kiribi.service.AbstractService;
import rs.igram.kiribi.service.Scope;
import rs.igram.kiribi.service.Service;
import rs.igram.kiribi.service.ServiceAddress;
import rs.igram.kiribi.service.Session;

/**
 * 
 *
 * @author Michael Sargent
 */
public class CalculatorService extends AbstractService {
	public CalculatorService(ServiceAddress address, Scope scope) {
		super(address, scope, new  Descriptor.Description("Calculator"));
	}
			
	@Override
	public Session newSession() {
		return new CalculatorSession(this);
	}
}